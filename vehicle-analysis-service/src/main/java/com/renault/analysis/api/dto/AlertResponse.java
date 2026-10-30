package com.renault.analysis.api.dto;

import com.renault.analysis.domain.model.Alert;

public record AlertResponse(String type, String severity, String message) {

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.type().name(), alert.severity().name(), alert.message());
    }
}