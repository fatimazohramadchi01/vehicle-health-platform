package com.renault.analysis.domain.model;

public record Alert(AlertType type, Severity severity, String message) {}