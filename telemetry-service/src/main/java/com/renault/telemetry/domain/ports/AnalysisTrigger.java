package com.renault.telemetry.domain.ports;

import com.renault.telemetry.domain.model.Telemetry;

public interface AnalysisTrigger {
    void trigger(Telemetry telemetry) throws AnalysisCallException;
}