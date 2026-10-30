package com.renault.telemetry.domain.ports;

import com.renault.telemetry.domain.model.AnalysisJob;
import java.time.Instant;
import java.util.List;

public interface AnalysisJobRepository {
    List<AnalysisJob> findDue(Instant now, int limit);
    void save(AnalysisJob job);
}