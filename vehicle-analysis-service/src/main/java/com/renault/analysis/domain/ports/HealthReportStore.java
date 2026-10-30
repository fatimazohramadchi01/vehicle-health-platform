package com.renault.analysis.domain.ports;

import com.renault.analysis.domain.model.StoredHealthReport;

import java.util.Optional;
import java.util.UUID;

public interface HealthReportStore {
    void save(StoredHealthReport report);
    Optional<StoredHealthReport> findByTelemetryId(UUID telemetryId);
    Optional<StoredHealthReport> findLatestByVin(String vin);
}