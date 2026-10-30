package com.renault.telemetry;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.renault.telemetry.application.ProcessPendingJobsUseCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "telemetry.scheduler.enabled=false",
        "telemetry.jobs.backoff-ms=0"})
@AutoConfigureMockMvc
class TelemetryIntegrationTest {

    private static final String VIN = "VF1RFA00567891234";
    private static final String ANALYSES = "/internal/v1/analyses";

    static final WireMockServer analysisService = new WireMockServer(options().dynamicPort());

    static {
        analysisService.start();
    }

    @DynamicPropertySource
    static void analysisServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("analysis-service.base-url", analysisService::baseUrl);
    }

    @AfterAll
    static void stop() {
        analysisService.stop();
    }

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProcessPendingJobsUseCase processJobs;

    @BeforeEach
    void reset() {
        analysisService.resetAll();
        jdbc.execute("DELETE FROM analysis_job");
        jdbc.execute("DELETE FROM telemetry");
    }

    @Test
    void validPayloadReturns202AndCreatesPendingJob() throws Exception {
        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(validPayload()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.telemetryId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ANALYSIS_PENDING"));

        assertThat(count("telemetry")).isEqualTo(1);
        assertThat(jobStatus()).isEqualTo("PENDING");
    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    void invalidPayloadReturns400AndStoresNothing(String payload) throws Exception {
        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        assertThat(count("telemetry")).isZero();
        assertThat(count("analysis_job")).isZero();
    }

    static Stream<String> invalidPayloads() {
        return Stream.of(
                validPayload().replace("\"vin\": \"" + VIN + "\"", "\"vin\": \"\""),
                validPayload().replace("\"vin\": \"" + VIN + "\"", "\"vin\": \"TOO-SHORT\""),
                validPayload().replace("[2.1, 2.1, 2.4, 2.4]", "[2.1, 2.1, 2.4]"),
                validPayload().replace("\"mileageKm\": 125000", "\"mileageKm\": -1"),
                validPayload().replace("\"fuelLevelPercent\": 8", "\"fuelLevelPercent\": 140"),
                "{ not json");
    }

    @Test
    void processingSendsTelemetryToAnalysisServiceAndMarksJobDone() throws Exception {
        stubAnalysis(200);
        postValidTelemetry();

        processJobs.process();

        analysisService.verify(1, postRequestedFor(urlPathEqualTo(ANALYSES))
                .withRequestBody(matchingJsonPath("$.vin", equalTo(VIN)))
                .withRequestBody(matchingJsonPath("$.measures.mileageKm", equalTo("125000")))
                .withRequestBody(matchingJsonPath("$.telemetryId")));
        assertThat(jobStatus()).isEqualTo("DONE");
    }

    @Test
    void analysisUnavailableIsRetriedThenJobBecomesDead() throws Exception {
        stubAnalysis(503);
        postValidTelemetry();

        processJobs.process();
        assertThat(jobStatus()).isEqualTo("FAILED");
        processJobs.process();
        processJobs.process();
        assertThat(jobStatus()).isEqualTo("DEAD");
        assertThat(jdbc.queryForObject("SELECT attempts FROM analysis_job", Integer.class)).isEqualTo(3);

        processJobs.process();
        analysisService.verify(3, postRequestedFor(urlPathEqualTo(ANALYSES)));
    }

    private void postValidTelemetry() throws Exception {
        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(validPayload()))
                .andExpect(status().isAccepted());
    }

    private void stubAnalysis(int statusCode) {
        analysisService.stubFor(WireMock.post(urlPathEqualTo(ANALYSES))
                .willReturn(aResponse().withStatus(statusCode)));
    }

    private Integer count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private String jobStatus() {
        return jdbc.queryForObject("SELECT status FROM analysis_job", String.class);
    }

    static String validPayload() {
        return """
                {
                  "vin": "%s",
                  "measuredAt": "2026-10-29T10:15:00Z",
                  "mileageKm": 125000,
                  "tirePressuresBar": [2.1, 2.1, 2.4, 2.4],
                  "batteryVoltage": 11.8,
                  "engineTemperatureC": 118,
                  "fuelLevelPercent": 8,
                  "errorCodes": ["P0301", "P0301"]
                }
                """.formatted(VIN);
    }
}