package com.renault.analysis;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisServiceIntegrationTest {

    private static final String VIN = "VF1RFA00567891234";
    private static final String TELEMETRY_ID = "d4f1c2e8-1234-5678-9abc-def012345678";
    private static final String TELEMETRY_ID_2 = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    static final WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());

    static {
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void recallProviderBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("recall-provider.base-url", wireMockServer::baseUrl);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDataAndDefaultStub() {
        wireMockServer.resetAll();
        stubOneActiveCampaign();
        jdbcTemplate.execute("DELETE FROM alert");
        jdbcTemplate.execute("DELETE FROM report_penalty");
        jdbcTemplate.execute("DELETE FROM health_report");
    }

    @Test
    void planExampleIsAnalyzedScoredAndExposed() throws Exception {
        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43))
                .andExpect(jsonPath("$.penalties", hasSize(7)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/" + VIN + "/health-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/" + VIN + "/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[?(@.type=='RECALL_CAMPAIGN')].severity").value(hasItem("CRITICAL")))
                .andExpect(jsonPath("$[?(@.type=='BATTERY_LOW')].severity").value(hasItem("HIGH")))
                .andExpect(jsonPath("$[?(@.type=='FUEL_LOW')].severity").value(hasItem("LOW")));
    }

    @Test
    void providerResponseImpactsScore() throws Exception {
        stubNoCampaigns();

        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(58));
    }

    @Test
    void providerFailurePreservesLastValidReport() throws Exception {
        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z")))
                .andExpect(status().isOk());

        stubProviderFailure();

        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planExampleRequest(TELEMETRY_ID_2, "2026-10-29T11:00:00Z")))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/" + VIN + "/health-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43));
        assertThat(reportCount()).isEqualTo(1);
    }

    @Test
    void replayingSameTelemetryIsIdempotent() throws Exception {
        String request = planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z");

        mockMvc.perform(post("/internal/v1/analyses").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43));
        mockMvc.perform(post("/internal/v1/analyses").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43));

        assertThat(reportCount()).isEqualTo(1);
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/recalls")));
    }

    @Test
    void latestMeasuredReportWins() throws Exception {
        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(healthyRequest(TELEMETRY_ID_2, "2026-10-29T08:00:00Z")))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/" + VIN + "/health-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(43))
                .andExpect(jsonPath("$.telemetryId").value(TELEMETRY_ID));
    }

    @Test
    void unknownVinReturns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/UNKNOWN12345/health-report"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPayloadReturns400() throws Exception {
        String invalid = planExampleRequest(TELEMETRY_ID, "2026-10-29T10:15:00Z")
                .replace("\"vin\": \"" + VIN + "\"", "\"vin\": \"\"");

        mockMvc.perform(post("/internal/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    private Integer reportCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM health_report", Integer.class);
    }

    private void stubOneActiveCampaign() {
        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/recalls"))
                .withQueryParam("vin", equalTo(VIN))
                .willReturn(okJson("""
                        {
                          "vin": "%s",
                          "campaigns": [
                            { "code": "RC-2026-014", "description": "Contrôle faisceau électrique", "status": "ACTIVE" }
                          ]
                        }
                        """.formatted(VIN))));
    }

    private void stubNoCampaigns() {
        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/recalls"))
                .withQueryParam("vin", equalTo(VIN))
                .willReturn(okJson("{ \"vin\": \"" + VIN + "\", \"campaigns\": [] }")));
    }

    private void stubProviderFailure() {
        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/recalls"))
                .withQueryParam("vin", equalTo(VIN))
                .willReturn(aResponse().withStatus(503)));
    }

    private String planExampleRequest(String telemetryId, String measuredAt) {
        return """
                {
                  "telemetryId": "%s",
                  "vin": "%s",
                  "measuredAt": "%s",
                  "measures": {
                    "mileageKm": 125000,
                    "tirePressuresBar": [2.1, 2.1, 2.4, 2.4],
                    "batteryVoltage": 11.8,
                    "engineTemperatureC": 118,
                    "fuelLevelPercent": 8,
                    "errorCodes": ["P0301", "P0301"]
                  }
                }
                """.formatted(telemetryId, VIN, measuredAt);
    }

    private String healthyRequest(String telemetryId, String measuredAt) {
        return """
                {
                  "telemetryId": "%s",
                  "vin": "%s",
                  "measuredAt": "%s",
                  "measures": {
                    "mileageKm": 50000,
                    "tirePressuresBar": [2.4, 2.4, 2.4, 2.4],
                    "batteryVoltage": 12.6,
                    "engineTemperatureC": 90,
                    "fuelLevelPercent": 60,
                    "errorCodes": []
                  }
                }
                """.formatted(telemetryId, VIN, measuredAt);
    }
}