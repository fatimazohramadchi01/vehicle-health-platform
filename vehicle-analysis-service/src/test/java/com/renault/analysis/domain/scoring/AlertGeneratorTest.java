package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.Alert;
import com.renault.analysis.domain.model.AlertType;
import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.RecallCampaign;
import com.renault.analysis.domain.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class AlertGeneratorTest {

    private final AlertGenerator alertGenerator = new AlertGenerator();
    private final HealthScoreCalculator calculator = new HealthScoreCalculator();

    @Test
    void healthyVehicleGeneratesNoAlerts() {
        List<Alert> alerts = alertGenerator.generate(new AnalysisContext(healthy().build(), List.of()), 100);

        assertThat(alerts).isEmpty();
    }

    @Test
    void planExampleGeneratesEightAlertsWithCorrectSeverities() {
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

        // On utilise le score réellement calculé, pas 43 en dur :
        // on teste l'assemblage calculateur -> alertes
        HealthReport report = calculator.calculate(context);
        List<Alert> alerts = alertGenerator.generate(context, report.score());

        assertThat(report.score()).isEqualTo(43);
        assertThat(alerts)
                .extracting(Alert::type, Alert::severity)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(AlertType.MILEAGE_HIGH, Severity.INFO),
                        org.assertj.core.groups.Tuple.tuple(AlertType.TIRE_PRESSURE_LOW, Severity.MEDIUM),
                        org.assertj.core.groups.Tuple.tuple(AlertType.BATTERY_LOW, Severity.HIGH),
                        org.assertj.core.groups.Tuple.tuple(AlertType.ENGINE_TEMPERATURE_HIGH, Severity.HIGH),
                        org.assertj.core.groups.Tuple.tuple(AlertType.FUEL_LOW, Severity.LOW),
                        org.assertj.core.groups.Tuple.tuple(AlertType.OBD_ERROR_CODE, Severity.HIGH),
                        org.assertj.core.groups.Tuple.tuple(AlertType.RECALL_CAMPAIGN, Severity.CRITICAL),
                        org.assertj.core.groups.Tuple.tuple(AlertType.HEALTH_SCORE_LOW, Severity.CRITICAL));
    }

    @Test
    void overInflatedTireTriggersHighAlertOnly() {
        AnalysisContext context = new AnalysisContext(
                healthy().withTirePressures(List.of(2.4, 2.4, 2.4, 2.9)).build(), List.of());

        List<Alert> alerts = alertGenerator.generate(context, 95);

        assertThat(alerts)
                .extracting(Alert::type, Alert::severity)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(AlertType.TIRE_PRESSURE_HIGH, Severity.MEDIUM));
    }

    @Test
    void batteryPenaltyWithoutAlert() {
        // 12,3 V : pénalité -5, mais aucune alerte (seuil d'alerte : 12,0 V strictement inférieur)
        AnalysisContext context = new AnalysisContext(healthy().withBatteryVoltage(12.3).build(), List.of());

        assertThat(alertGenerator.generate(context, 95)).isEmpty();
    }

    @Test
    void alertThresholdsAreStrictBoundaries() {
        // Batterie : 12,0 V -> rien ; 11,9 V -> BATTERY_LOW
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().withBatteryVoltage(12.0).build(), List.of()), 100)).isEmpty();
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().withBatteryVoltage(11.9).build(), List.of()), 100))
                .extracting(Alert::type).containsExactly(AlertType.BATTERY_LOW);

        // Température : 115 °C -> rien ; 116 °C -> ENGINE_TEMPERATURE_HIGH
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().withEngineTemperatureC(115).build(), List.of()), 100)).isEmpty();
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().withEngineTemperatureC(116).build(), List.of()), 100))
                .extracting(Alert::type).containsExactly(AlertType.ENGINE_TEMPERATURE_HIGH);

        // Score : 50 -> rien ; 49 -> HEALTH_SCORE_LOW
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().build(), List.of()), 50)).isEmpty();
        assertThat(alertGenerator.generate(new AnalysisContext(healthy().build(), List.of()), 49))
                .extracting(Alert::type).containsExactly(AlertType.HEALTH_SCORE_LOW);
    }
}