package com.renault.telemetry.api.dto;

import java.util.UUID;

public record TelemetryAcceptedResponse(UUID telemetryId, String status) {
    public static TelemetryAcceptedResponse pending(UUID id) {
        return new TelemetryAcceptedResponse(id, "ANALYSIS_PENDING");
    }
}