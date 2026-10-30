package com.renault.telemetry.domain.ports;

import com.renault.telemetry.domain.model.AnalysisJob;
import com.renault.telemetry.domain.model.Telemetry;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository {
    void saveWithJob(Telemetry telemetry, AnalysisJob job);   // une seule transaction
    Optional<Telemetry> findById(UUID id);
}