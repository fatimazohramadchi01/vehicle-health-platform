package com.renault.telemetry.infrastructure.jpa;

import com.renault.telemetry.domain.model.AnalysisJob;
import com.renault.telemetry.domain.model.JobStatus;
import com.renault.telemetry.domain.ports.AnalysisJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
class JpaAnalysisJobRepository implements AnalysisJobRepository {

    private final SpringDataAnalysisJobRepository jobs;

    JpaAnalysisJobRepository(SpringDataAnalysisJobRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisJob> findDue(Instant now, int limit) {
        return jobs.findByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                        List.of(JobStatus.PENDING, JobStatus.FAILED), now, PageRequest.of(0, limit))
                .stream().map(JpaAnalysisJobRepository::toDomain).toList();
    }

    @Override
    @Transactional
    public void save(AnalysisJob job) {
        jobs.save(toEntity(job));
    }

    static AnalysisJobEntity toEntity(AnalysisJob j) {
        AnalysisJobEntity e = new AnalysisJobEntity();
        e.id = j.id();
        e.telemetryId = j.telemetryId();
        e.status = j.status();
        e.attempts = j.attempts();
        e.nextAttemptAt = j.nextAttemptAt();
        e.lastError = j.lastError() == null ? null
                : j.lastError().substring(0, Math.min(1000, j.lastError().length()));
        return e;
    }

    private static AnalysisJob toDomain(AnalysisJobEntity e) {
        return new AnalysisJob(e.id, e.telemetryId, e.status, e.attempts, e.nextAttemptAt, e.lastError);
    }
}