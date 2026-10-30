package com.renault.telemetry.infrastructure.http;

import com.renault.telemetry.domain.model.Telemetry;
import com.renault.telemetry.domain.ports.AnalysisCallException;
import com.renault.telemetry.domain.ports.AnalysisTrigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
class HttpAnalysisTrigger implements AnalysisTrigger {

    private final RestClient client;

    HttpAnalysisTrigger(RestClient.Builder builder, @Value("${analysis-service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public void trigger(Telemetry t) {
        try {
            client.post().uri("/internal/v1/analyses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(AnalysisPayload.from(t))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new AnalysisCallException("HTTP " + e.getStatusCode().value() + " from analysis-service",
                    isRetryable(e.getStatusCode()), e);
        } catch (ResourceAccessException e) {
            throw new AnalysisCallException("analysis-service unreachable: " + e.getMessage(), true, e);
        }
    }

    private static boolean isRetryable(HttpStatusCode status) {
        return status.is5xxServerError() || status.value() == 408 || status.value() == 429;
    }

    record AnalysisPayload(UUID telemetryId, String vin, Instant measuredAt, Measures measures) {
        record Measures(int mileageKm, List<Double> tirePressuresBar, double batteryVoltage,
                        double engineTemperatureC, double fuelLevelPercent, List<String> errorCodes) {}

        static AnalysisPayload from(Telemetry t) {
            return new AnalysisPayload(t.id(), t.vin(), t.measuredAt(),
                    new Measures(t.mileageKm(), t.tirePressuresBar(), t.batteryVoltage(),
                            t.engineTemperatureC(), t.fuelLevelPercent(), t.errorCodes()));
        }
    }
}