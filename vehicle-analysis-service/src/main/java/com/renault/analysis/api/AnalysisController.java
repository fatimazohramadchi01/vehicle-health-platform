package com.renault.analysis.api;

import com.renault.analysis.api.dto.AnalysisRequest;
import com.renault.analysis.api.dto.HealthReportResponse;
import com.renault.analysis.application.AnalyzeVehicleCommand;
import com.renault.analysis.application.AnalyzeVehicleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/analyses")
public class AnalysisController {

    private final AnalyzeVehicleUseCase analyzeVehicleUseCase;

    public AnalysisController(AnalyzeVehicleUseCase analyzeVehicleUseCase) {
        this.analyzeVehicleUseCase = analyzeVehicleUseCase;
    }

    @PostMapping
    public ResponseEntity<HealthReportResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
        AnalyzeVehicleCommand command = new AnalyzeVehicleCommand(
                request.telemetryId(),
                request.vin(),
                request.measuredAt(),
                request.measures().mileageKm(),
                request.measures().tirePressuresBar(),
                request.measures().batteryVoltage(),
                request.measures().engineTemperatureC(),
                request.measures().fuelLevelPercent(),
                request.measures().errorCodes());
        return ResponseEntity.ok(HealthReportResponse.from(analyzeVehicleUseCase.analyze(command)));
    }
}