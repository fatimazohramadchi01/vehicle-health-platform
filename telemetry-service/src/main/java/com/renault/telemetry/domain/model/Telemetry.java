package com.renault.telemetry.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Telemetry(UUID id, String vin, Instant measuredAt, int mileageKm,
                        List<Double> tirePressuresBar, double batteryVoltage,
                        double engineTemperatureC, double fuelLevelPercent,
                        List<String> errorCodes) {}