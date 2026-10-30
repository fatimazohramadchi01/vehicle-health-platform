package com.renault.telemetry.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record AnalysisJob(UUID id, UUID telemetryId, JobStatus status, int attempts,
                          Instant nextAttemptAt, String lastError) {

    public static final int MAX_ATTEMPTS = 3;

    public static AnalysisJob pending(UUID telemetryId, Instant now) {
        return new AnalysisJob(UUID.randomUUID(), telemetryId, JobStatus.PENDING, 0, now, null);
    }

    public AnalysisJob succeeded() {
        return new AnalysisJob(id, telemetryId, JobStatus.DONE, attempts + 1, nextAttemptAt, null);
    }

    public AnalysisJob failed(String error, boolean retryable, Instant now, Duration backoff) {
        int count = attempts + 1;
        if (!retryable || count >= MAX_ATTEMPTS) {
            return new AnalysisJob(id, telemetryId, JobStatus.DEAD, count, nextAttemptAt, error);
        }
        return new AnalysisJob(id, telemetryId, JobStatus.FAILED, count,
                now.plus(backoff.multipliedBy(count)), error);
    }
}