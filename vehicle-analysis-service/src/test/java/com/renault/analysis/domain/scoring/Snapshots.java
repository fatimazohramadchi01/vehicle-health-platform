package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.VehicleSnapshot;

import java.time.Instant;
import java.util.List;

final class Snapshots {

    static Builder healthy() {
        return new Builder();
    }

    static final class Builder {
        private String vin = "VF1TEST0000000001";
        private Instant measuredAt = Instant.parse("2026-10-29T10:15:00Z");
        private long mileageKm = 50_000;
        private List<Double> tirePressuresBar = List.of(2.4, 2.4, 2.4, 2.4);
        private double batteryVoltage = 12.6;
        private double engineTemperatureC = 90;
        private int fuelLevelPercent = 60;
        private List<String> errorCodes = List.of();

        Builder withMileageKm(long v) { this.mileageKm = v; return this; }
        Builder withTirePressures(List<Double> v) { this.tirePressuresBar = v; return this; }
        Builder withBatteryVoltage(double v) { this.batteryVoltage = v; return this; }
        Builder withEngineTemperatureC(double v) { this.engineTemperatureC = v; return this; }
        Builder withFuelLevelPercent(int v) { this.fuelLevelPercent = v; return this; }
        Builder withErrorCodes(List<String> v) { this.errorCodes = v; return this; }

        VehicleSnapshot build() {
            return new VehicleSnapshot(vin, measuredAt, mileageKm, tirePressuresBar,
                    batteryVoltage, engineTemperatureC, fuelLevelPercent, errorCodes);
        }
    }
}