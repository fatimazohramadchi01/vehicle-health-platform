package com.renault.telemetry.application;

import com.renault.telemetry.domain.model.AnalysisJob;
import com.renault.telemetry.domain.model.JobStatus;
import com.renault.telemetry.domain.model.Telemetry;
import com.renault.telemetry.domain.ports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ProcessPendingJobsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPendingJobsUseCase.class);

    private final AnalysisJobRepository jobs;
    private final TelemetryRepository telemetries;
    private final AnalysisTrigger trigger;
    private final Clock clock;
    private final Duration backoff;
    private final int batchSize;

    public ProcessPendingJobsUseCase(AnalysisJobRepository jobs, TelemetryRepository telemetries,
                                     AnalysisTrigger trigger, Clock clock,
                                     Duration backoff, int batchSize) {
        this.jobs = jobs;
        this.telemetries = telemetries;
        this.trigger = trigger;
        this.clock = clock;
        this.backoff = backoff;
        this.batchSize = batchSize;
    }

    public int process() {
        Instant now = clock.instant();
        List<AnalysisJob> due = jobs.findDue(now, batchSize);
        due.forEach(job -> handle(job, now));
        return due.size();
    }

    private void handle(AnalysisJob job, Instant now) {
        Optional<Telemetry> telemetry = telemetries.findById(job.telemetryId());
        if (telemetry.isEmpty()) {
            jobs.save(job.failed("Telemetry not found", false, now, backoff));
            return;
        }
        try {
            trigger.trigger(telemetry.get());
            jobs.save(job.succeeded());
        } catch (AnalysisCallException e) {
            AnalysisJob updated = job.failed(e.getMessage(), e.isRetryable(), now, backoff);
            jobs.save(updated);
            if (updated.status() == JobStatus.DEAD) {
                log.error("Job {} abandoned after {} attempts: {}", job.id(), updated.attempts(), e.getMessage());
            } else {
                log.warn("Job {} failed (attempt {}): {}", job.id(), updated.attempts(), e.getMessage());
            }
        }
    }
}