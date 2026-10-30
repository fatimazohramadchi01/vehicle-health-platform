package com.renault.telemetry.api.dto;

import com.renault.telemetry.application.IngestTelemetryCommand;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record TelemetryRequest(
        @NotBlank @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be 17 characters (no I, O, Q)") String vin,
        @NotNull Instant measuredAt,
        @NotNull @PositiveOrZero Integer mileageKm,
        @NotNull @Size(min = 4, max = 4, message = "exactly 4 tire pressures required")
        List<@NotNull @Positive Double> tirePressuresBar,
        @NotNull @PositiveOrZero Double batteryVoltage,
        @NotNull Double engineTemperatureC,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double fuelLevelPercent,
        @NotNull List<@NotBlank String> errorCodes) {

    public IngestTelemetryCommand toCommand() {
        return new IngestTelemetryCommand(vin, measuredAt, mileageKm, tirePressuresBar,
                batteryVoltage, engineTemperatureC, fuelLevelPercent, errorCodes);
    }
}