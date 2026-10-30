package com.renault.telemetry.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisJobTest {

    private static final Instant NOW = Instant.parse("2026-10-29T10:00:00Z");
    private static final Duration BACKOFF = Duration.ofSeconds(2);

    private AnalysisJob fresh() {
        return AnalysisJob.pending(UUID.randomUUID(), NOW);
    }

    @Test
    void firstRetryableFailureMovesToFailedWithBackoff() {
        AnalysisJob job = fresh().failed("503", true, NOW, BACKOFF);
        assertThat(job.status()).isEqualTo(JobStatus.FAILED);
        assertThat(job.attempts()).isEqualTo(1);
        assertThat(job.nextAttemptAt()).isEqualTo(NOW.plusSeconds(2));
    }

    @Test
    void thirdFailureMovesToDead() {
        AnalysisJob job = fresh().failed("e", true, NOW, BACKOFF)
                .failed("e", true, NOW, BACKOFF)
                .failed("e", true, NOW, BACKOFF);
        assertThat(job.status()).isEqualTo(JobStatus.DEAD);
        assertThat(job.attempts()).isEqualTo(3);
    }

    @Test
    void nonRetryableFailureIsDeadImmediately() {
        assertThat(fresh().failed("400", false, NOW, BACKOFF).status()).isEqualTo(JobStatus.DEAD);
    }

    @Test
    void successMovesToDone() {
        AnalysisJob job = fresh().succeeded();
        assertThat(job.status()).isEqualTo(JobStatus.DONE);
        assertThat(job.attempts()).isEqualTo(1);
    }
}