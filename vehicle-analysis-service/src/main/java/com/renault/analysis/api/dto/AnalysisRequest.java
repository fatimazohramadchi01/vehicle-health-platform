package com.renault.analysis.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisRequest(
        @NotNull UUID telemetryId,
        @NotBlank String vin,
        @NotNull Instant measuredAt,
        @NotNull @Valid Measures measures
) {
    public record Measures(
            @NotNull @PositiveOrZero Long mileageKm,
            @NotNull @Size(min = 4, max = 4) List<@NotNull Double> tirePressuresBar,
            @NotNull Double batteryVoltage,
            @NotNull Double engineTemperatureC,
            @NotNull @Min(0) @Max(100) Integer fuelLevelPercent,
            @NotNull List<@NotBlank String> errorCodes
    ) {}
}