package com.renault.telemetry.infrastructure.jpa;

import com.renault.telemetry.domain.model.JobStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_job")
class AnalysisJobEntity {
    @Id UUID id;
    @Column(name = "telemetry_id", nullable = false, unique = true) UUID telemetryId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) JobStatus status;
    int attempts;
    @Column(name = "next_attempt_at") Instant nextAttemptAt;
    @Column(name = "last_error", length = 1000) String lastError;

    protected AnalysisJobEntity() {}
}