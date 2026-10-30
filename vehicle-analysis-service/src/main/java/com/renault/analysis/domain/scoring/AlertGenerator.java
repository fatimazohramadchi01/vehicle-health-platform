package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.Alert;
import com.renault.analysis.domain.model.AlertType;
import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Severity;
import com.renault.analysis.domain.model.VehicleSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlertGenerator {

    // Attention : seuils d'alerte VOLONTAIREMENT différents des seuils de pénalité
    public List<Alert> generate(AnalysisContext context, int score) {
        VehicleSnapshot snapshot = context.snapshot();
        List<Alert> alerts = new ArrayList<>();

        if (snapshot.mileageKm() >= 125_000) {
            alerts.add(new Alert(AlertType.MILEAGE_HIGH, Severity.INFO,
                    "Kilométrage élevé : " + snapshot.mileageKm() + " km"));
        }
        if (snapshot.tirePressuresBar().stream().anyMatch(p -> p < 2.2)) {
            alerts.add(new Alert(AlertType.TIRE_PRESSURE_LOW, Severity.MEDIUM,
                    "Au moins un pneu sous-gonflé"));
        }
        if (snapshot.tirePressuresBar().stream().anyMatch(p -> p > 2.8)) {
            alerts.add(new Alert(AlertType.TIRE_PRESSURE_HIGH, Severity.MEDIUM,
                    "Au moins un pneu sur-gonflé"));
        }
        if (snapshot.batteryVoltage() < 12.0) {
            alerts.add(new Alert(AlertType.BATTERY_LOW, Severity.HIGH,
                    "Batterie 12 V faible : " + snapshot.batteryVoltage() + " V"));
        }
        if (snapshot.engineTemperatureC() > 115) {
            alerts.add(new Alert(AlertType.ENGINE_TEMPERATURE_HIGH, Severity.HIGH,
                    "Température moteur élevée : " + snapshot.engineTemperatureC() + " °C"));
        }
        if (snapshot.fuelLevelPercent() < 10) {
            alerts.add(new Alert(AlertType.FUEL_LOW, Severity.LOW,
                    "Niveau de carburant bas : " + snapshot.fuelLevelPercent() + " %"));
        }
        if (!snapshot.errorCodes().isEmpty()) {
            alerts.add(new Alert(AlertType.OBD_ERROR_CODE, Severity.HIGH,
                    "Codes erreur détectés : " + snapshot.errorCodes()));
        }
        if (!context.activeRecallCampaigns().isEmpty()) {
            alerts.add(new Alert(AlertType.RECALL_CAMPAIGN, Severity.CRITICAL,
                    "Campagne(s) de rappel active(s) : " + context.activeRecallCampaigns().size()));
        }
        if (score < 50) {
            alerts.add(new Alert(AlertType.HEALTH_SCORE_LOW, Severity.CRITICAL,
                    "Health score critique : " + score + "/100"));
        }

        return List.copyOf(alerts);
    }
}