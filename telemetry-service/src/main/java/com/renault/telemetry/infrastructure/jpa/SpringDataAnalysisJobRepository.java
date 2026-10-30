package com.renault.telemetry.infrastructure.jpa;

import com.renault.telemetry.domain.model.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface SpringDataAnalysisJobRepository extends JpaRepository<AnalysisJobEntity, UUID> {
    List<AnalysisJobEntity> findByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            Collection<JobStatus> statuses, Instant now, Pageable pageable);
}