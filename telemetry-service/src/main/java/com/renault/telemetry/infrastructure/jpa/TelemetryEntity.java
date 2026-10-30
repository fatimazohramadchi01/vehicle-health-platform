package com.renault.telemetry.infrastructure.jpa;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "telemetry", indexes = @Index(name = "idx_telemetry_vin", columnList = "vin"))
class TelemetryEntity {
    @Id UUID id;
    @Column(nullable = false) String vin;
    @Column(name = "measured_at", nullable = false) Instant measuredAt;
    @Column(name = "mileage_km", nullable = false) int mileageKm;
    @Convert(converter = DoubleListConverter.class)
    @Column(name = "tire_pressures", length = 200) List<Double> tirePressures;
    @Column(name = "battery_voltage") double batteryVoltage;
    @Column(name = "engine_temp_c") double engineTempC;
    @Column(name = "fuel_level_pct") double fuelLevelPct;
    @Convert(converter = StringListConverter.class)
    @Column(name = "error_codes", length = 1000) List<String> errorCodes;

    protected TelemetryEntity() {}
}