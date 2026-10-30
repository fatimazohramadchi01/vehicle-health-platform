package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.RecallCampaign;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class HealthScoreCalculatorTest {

    private final HealthScoreCalculator calculator = new HealthScoreCalculator();

    @Test
    void healthyVehicleScores100() {
        HealthReport report = calculator.calculate(new AnalysisContext(healthy().build(), List.of()));

        assertThat(report.score()).isEqualTo(100);
        assertThat(report.penalties()).isEmpty();
    }

    @Test
    void planExampleScores43() {
        AnalysisContext context = new AnalysisContext(
                healthy()
                        .withMileageKm(125_000)
                        .withTirePressures(List.of(2.1, 2.1, 2.4, 2.4))
                        .withBatteryVoltage(11.8)
                        .withEngineTemperatureC(118)
                        .withFuelLevelPercent(8)
                        .withErrorCodes(List.of("P0301", "P0301"))
                        .build(),
                List.of(new RecallCampaign("RC-2026-014", "Contrôle faisceau électrique")));

        HealthReport report = calculator.calculate(context);

        assertThat(report.score()).isEqualTo(43);
        assertThat(report.penalties()).hasSize(7);
        assertThat(report.penalties().stream().mapToInt(Penalty::points).sum()).isEqualTo(-57);
    }

    @Test
    void floorAtZeroForExtremelyDamagedVehicle() {
        AnalysisContext context = new AnalysisContext(
                healthy()
                        .withMileageKm(300_000)
                        .withTirePressures(List.of(2.0, 2.0, 2.0, 2.0))
                        .withBatteryVoltage(11.0)
                        .withEngineTemperatureC(130)
                        .withFuelLevelPercent(3)
                        .withErrorCodes(List.of("P0301", "P0171", "P0420", "P0128", "P0455"))
                        .build(),
                List.of(new RecallCampaign("RC-1", "desc")));

        assertThat(calculator.calculate(context).score()).isZero();
    }
}