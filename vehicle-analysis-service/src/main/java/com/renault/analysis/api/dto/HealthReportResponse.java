package com.renault.analysis.api.dto;

import com.renault.analysis.domain.model.StoredHealthReport;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HealthReportResponse(UUID telemetryId, String vin, Instant measuredAt, int score,
                                   List<PenaltyResponse> penalties) {

    public static HealthReportResponse from(StoredHealthReport stored) {
        return new HealthReportResponse(
                stored.telemetryId(),
                stored.report().vin(),
                stored.report().measuredAt(),
                stored.report().score(),
                stored.report().penalties().stream()
                        .map(p -> new PenaltyResponse(p.rule().name(), p.points()))
                        .toList());
    }

    public record PenaltyResponse(String rule, int points) {}
}