package com.renault.analysis.application;

public class RecallProviderUnavailableException extends RuntimeException {

    public RecallProviderUnavailableException(String message) {
        super(message);
    }

    public RecallProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}