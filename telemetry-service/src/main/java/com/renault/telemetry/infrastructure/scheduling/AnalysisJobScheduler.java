package com.renault.telemetry.infrastructure.scheduling;

import com.renault.telemetry.application.ProcessPendingJobsUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "telemetry.scheduler.enabled", havingValue = "true", matchIfMissing = true)
class AnalysisJobScheduler {

    private final ProcessPendingJobsUseCase useCase;

    AnalysisJobScheduler(ProcessPendingJobsUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(fixedDelayString = "${telemetry.scheduler.poll-interval-ms:2000}")
    void poll() {
        useCase.process();
    }
}