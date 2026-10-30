package com.renault.telemetry.api;

import com.renault.telemetry.api.dto.TelemetryAcceptedResponse;
import com.renault.telemetry.api.dto.TelemetryRequest;
import com.renault.telemetry.application.IngestTelemetryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final IngestTelemetryUseCase useCase;

    public TelemetryController(IngestTelemetryUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<TelemetryAcceptedResponse> ingest(@Valid @RequestBody TelemetryRequest request) {
        return ResponseEntity.accepted()
                .body(TelemetryAcceptedResponse.pending(useCase.ingest(request.toCommand())));
    }
}