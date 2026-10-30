package com.renault.telemetry.domain.ports;

public class AnalysisCallException extends RuntimeException {
    private final boolean retryable;

    public AnalysisCallException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() { return retryable; }
}