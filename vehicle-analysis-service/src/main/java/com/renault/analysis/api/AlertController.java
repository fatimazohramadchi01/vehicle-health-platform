package com.renault.analysis.api;

import com.renault.analysis.api.dto.AlertResponse;
import com.renault.analysis.domain.model.StoredHealthReport;
import com.renault.analysis.domain.ports.HealthReportStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles/{vin}")
public class AlertController {

    private final HealthReportStore reportStore;

    public AlertController(HealthReportStore reportStore) {
        this.reportStore = reportStore;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertResponse>> alerts(@PathVariable("vin") String vin) {
        return reportStore.findLatestByVin(vin)
                .map(AlertController::toResponses)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static List<AlertResponse> toResponses(StoredHealthReport stored) {
        return stored.alerts().stream().map(AlertResponse::from).toList();
    }
}