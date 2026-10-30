package com.renault.telemetry.infrastructure.jpa;

import com.renault.telemetry.domain.model.AnalysisJob;
import com.renault.telemetry.domain.model.Telemetry;
import com.renault.telemetry.domain.ports.TelemetryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
class JpaTelemetryRepository implements TelemetryRepository {

    private final SpringDataTelemetryRepository telemetries;
    private final SpringDataAnalysisJobRepository jobs;

    JpaTelemetryRepository(SpringDataTelemetryRepository telemetries, SpringDataAnalysisJobRepository jobs) {
        this.telemetries = telemetries;
        this.jobs = jobs;
    }

    @Override
    @Transactional
    public void saveWithJob(Telemetry t, AnalysisJob job) {
        telemetries.save(toEntity(t));
        jobs.save(JpaAnalysisJobRepository.toEntity(job));

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Telemetry> findById(UUID id) {
        return telemetries.findById(id).map(JpaTelemetryRepository::toDomain);
    }

    private static TelemetryEntity toEntity(Telemetry t) {
        TelemetryEntity e = new TelemetryEntity();
        e.id = t.id();
        e.vin = t.vin();
        e.measuredAt = t.measuredAt();
        e.mileageKm = t.mileageKm();
        e.tirePressures = t.tirePressuresBar();
        e.batteryVoltage = t.batteryVoltage();
        e.engineTempC = t.engineTemperatureC();
        e.fuelLevelPct = t.fuelLevelPercent();
        e.errorCodes = t.errorCodes();
        return e;
    }

    private static Telemetry toDomain(TelemetryEntity e) {
        return new Telemetry(e.id, e.vin, e.measuredAt, e.mileageKm, e.tirePressures,
                e.batteryVoltage, e.engineTempC, e.fuelLevelPct, e.errorCodes);
    }
}