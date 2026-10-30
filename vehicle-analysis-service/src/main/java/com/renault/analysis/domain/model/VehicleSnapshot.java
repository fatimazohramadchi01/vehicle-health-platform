package com.renault.analysis.domain.model;

import java.time.Instant;
import java.util.List;

public record VehicleSnapshot(
        String vin,
        Instant measuredAt,
        long mileageKm,
        List<Double> tirePressuresBar,
        double batteryVoltage,
        double engineTemperatureC,
        int fuelLevelPercent,
        List<String> errorCodes
) {}