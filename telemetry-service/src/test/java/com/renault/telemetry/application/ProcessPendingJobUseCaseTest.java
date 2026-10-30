package com.renault.telemetry.application;

import com.renault.telemetry.domain.model.*;
import com.renault.telemetry.domain.ports.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessPendingJobsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-10-29T10:00:00Z");

    private final AnalysisJobRepository jobs = mock(AnalysisJobRepository.class);
    private final TelemetryRepository telemetries = mock(TelemetryRepository.class);
    private final AnalysisTrigger trigger = mock(AnalysisTrigger.class);
    private final ProcessPendingJobsUseCase useCase = new ProcessPendingJobsUseCase(
            jobs, telemetries, trigger, Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofSeconds(2), 10);

    private final UUID telemetryId = UUID.randomUUID();
    private final AnalysisJob job = AnalysisJob.pending(telemetryId, NOW);
    private final Telemetry telemetry = new Telemetry(telemetryId, "VF1RFA00567891234", NOW, 125000,
            List.of(2.1, 2.1, 2.4, 2.4), 11.8, 118, 8, List.of("P0301"));

    private AnalysisJob savedJob() {
        ArgumentCaptor<AnalysisJob> captor = ArgumentCaptor.forClass(AnalysisJob.class);
        verify(jobs).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void successfulCallMarksJobDone() {
        when(jobs.findDue(NOW, 10)).thenReturn(List.of(job));
        when(telemetries.findById(telemetryId)).thenReturn(Optional.of(telemetry));

        useCase.process();

        verify(trigger).trigger(telemetry);
        assertThat(savedJob().status()).isEqualTo(JobStatus.DONE);
    }

    @Test
    void retryableFailureMarksJobFailed() {
        when(jobs.findDue(NOW, 10)).thenReturn(List.of(job));
        when(telemetries.findById(telemetryId)).thenReturn(Optional.of(telemetry));
        doThrow(new AnalysisCallException("HTTP 503", true, null)).when(trigger).trigger(any());

        useCase.process();

        assertThat(savedJob().status()).isEqualTo(JobStatus.FAILED);
    }

    @Test
    void missingTelemetryKillsJobWithoutCallingAnalysis() {
        when(jobs.findDue(NOW, 10)).thenReturn(List.of(job));
        when(telemetries.findById(telemetryId)).thenReturn(Optional.empty());

        useCase.process();

        verifyNoInteractions(trigger);
        assertThat(savedJob().status()).isEqualTo(JobStatus.DEAD);
    }
}