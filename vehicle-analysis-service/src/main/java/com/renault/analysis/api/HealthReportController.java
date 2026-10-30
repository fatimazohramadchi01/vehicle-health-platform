package com.renault.analysis.api;

import com.renault.analysis.api.dto.HealthReportResponse;
import com.renault.analysis.domain.ports.HealthReportStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vin}")
public class HealthReportController {

    private final HealthReportStore reportStore;

    public HealthReportController(HealthReportStore reportStore) {
        this.reportStore = reportStore;
    }

    @GetMapping("/health-report")
    public ResponseEntity<HealthReportResponse> healthReport(@PathVariable("vin") String vin) {
        return reportStore.findLatestByVin(vin)
                .map(HealthReportResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}