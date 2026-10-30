package com.renault.analysis.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalyzeVehicleCommand(
        UUID telemetryId,
        String vin,
        Instant measuredAt,
        long mileageKm,
        List<Double> tirePressuresBar,
        double batteryVoltage,
        double engineTemperatureC,
        int fuelLevelPercent,
        List<String> errorCodes
) {}