package com.renault.telemetry.infrastructure;

import com.renault.telemetry.application.IngestTelemetryUseCase;
import com.renault.telemetry.application.ProcessPendingJobsUseCase;
import com.renault.telemetry.domain.ports.AnalysisJobRepository;
import com.renault.telemetry.domain.ports.AnalysisTrigger;
import com.renault.telemetry.domain.ports.TelemetryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableScheduling
public class TelemetryConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    IngestTelemetryUseCase ingestTelemetryUseCase(TelemetryRepository repository, Clock clock) {
        return new IngestTelemetryUseCase(repository, clock);
    }

    @Bean
    ProcessPendingJobsUseCase processPendingJobsUseCase(AnalysisJobRepository jobs,
                                                        TelemetryRepository telemetries,
                                                        AnalysisTrigger trigger, Clock clock,
                                                        @Value("${telemetry.jobs.backoff-ms:2000}") long backoffMs,
                                                        @Value("${telemetry.jobs.batch-size:20}") int batchSize) {
        return new ProcessPendingJobsUseCase(jobs, telemetries, trigger, clock,
                Duration.ofMillis(backoffMs), batchSize);
    }
}