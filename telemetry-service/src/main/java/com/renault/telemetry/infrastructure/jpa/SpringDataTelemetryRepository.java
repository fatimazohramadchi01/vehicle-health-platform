package com.renault.telemetry.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface SpringDataTelemetryRepository extends JpaRepository<TelemetryEntity, UUID> {}