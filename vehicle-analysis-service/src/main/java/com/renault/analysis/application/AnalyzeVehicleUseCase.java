package com.renault.analysis.application;

import com.renault.analysis.domain.model.Alert;
import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.RecallCampaign;
import com.renault.analysis.domain.model.StoredHealthReport;
import com.renault.analysis.domain.model.VehicleSnapshot;
import com.renault.analysis.domain.ports.HealthReportStore;
import com.renault.analysis.domain.ports.RecallProvider;
import com.renault.analysis.domain.scoring.AlertGenerator;
import com.renault.analysis.domain.scoring.HealthScoreCalculator;

import java.util.List;
import java.util.Optional;

public class AnalyzeVehicleUseCase {

    private final RecallProvider recallProvider;
    private final HealthReportStore reportStore;
    private final HealthScoreCalculator scoreCalculator;
    private final AlertGenerator alertGenerator;

    public AnalyzeVehicleUseCase(RecallProvider recallProvider,
                                 HealthReportStore reportStore,
                                 HealthScoreCalculator scoreCalculator,
                                 AlertGenerator alertGenerator) {
        this.recallProvider = recallProvider;
        this.reportStore = reportStore;
        this.scoreCalculator = scoreCalculator;
        this.alertGenerator = alertGenerator;
    }

    public StoredHealthReport analyze(AnalyzeVehicleCommand command) {
        Optional<StoredHealthReport> existing = reportStore.findByTelemetryId(command.telemetryId());
        if (existing.isPresent()) {
            return existing.get(); // idempotence : rejeu, rien à recalculer
        }

        List<RecallCampaign> campaigns = recallProvider.activeCampaignsFor(command.vin());

        VehicleSnapshot snapshot = new VehicleSnapshot(
                command.vin(),
                command.measuredAt(),
                command.mileageKm(),
                command.tirePressuresBar(),
                command.batteryVoltage(),
                command.engineTemperatureC(),
                command.fuelLevelPercent(),
                command.errorCodes());
        AnalysisContext context = new AnalysisContext(snapshot, campaigns);

        HealthReport report = scoreCalculator.calculate(context);
        List<Alert> alerts = alertGenerator.generate(context, report.score());

        StoredHealthReport stored = new StoredHealthReport(command.telemetryId(), report, alerts);
        reportStore.save(stored);
        return stored;
    }
}