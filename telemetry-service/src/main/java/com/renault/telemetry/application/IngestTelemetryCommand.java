package com.renault.telemetry.application;

import java.time.Instant;
import java.util.List;

public record IngestTelemetryCommand(String vin, Instant measuredAt, int mileageKm,
                                     List<Double> tirePressuresBar, double batteryVoltage,
                                     double engineTemperatureC, double fuelLevelPercent,
                                     List<String> errorCodes) {}