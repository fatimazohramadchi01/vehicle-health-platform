package com.renault.telemetry.application;

import com.renault.telemetry.domain.model.AnalysisJob;
import com.renault.telemetry.domain.model.Telemetry;
import com.renault.telemetry.domain.ports.TelemetryRepository;

import java.time.Clock;
import java.util.UUID;

public class IngestTelemetryUseCase {

    private final TelemetryRepository repository;
    private final Clock clock;

    public IngestTelemetryUseCase(TelemetryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public UUID ingest(IngestTelemetryCommand c) {
        UUID id = UUID.randomUUID();
        Telemetry telemetry = new Telemetry(id, c.vin(), c.measuredAt(), c.mileageKm(),
                c.tirePressuresBar(), c.batteryVoltage(), c.engineTemperatureC(),
                c.fuelLevelPercent(), c.errorCodes());
        repository.saveWithJob(telemetry, AnalysisJob.pending(id, clock.instant()));
        return id;
    }
}