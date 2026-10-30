package com.renault.analysis.infrastructure;

import com.renault.analysis.application.AnalyzeVehicleUseCase;
import com.renault.analysis.domain.ports.HealthReportStore;
import com.renault.analysis.domain.ports.RecallProvider;
import com.renault.analysis.domain.scoring.AlertGenerator;
import com.renault.analysis.domain.scoring.HealthScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VehicleAnalysisConfiguration {

    @Bean
    public AnalyzeVehicleUseCase analyzeVehicleUseCase(RecallProvider recallProvider, HealthReportStore healthReportStore) {
        return new AnalyzeVehicleUseCase(recallProvider, healthReportStore,
                new HealthScoreCalculator(), new AlertGenerator());
    }
}