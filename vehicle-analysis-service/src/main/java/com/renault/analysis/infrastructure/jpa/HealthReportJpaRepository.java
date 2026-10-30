package com.renault.analysis.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HealthReportJpaRepository extends JpaRepository<HealthReportJpaEntity, UUID> {
    Optional<HealthReportJpaEntity> findByTelemetryId(UUID telemetryId);
    Optional<HealthReportJpaEntity> findTopByVinOrderByMeasuredAtDescTelemetryIdDesc(String vin);
}