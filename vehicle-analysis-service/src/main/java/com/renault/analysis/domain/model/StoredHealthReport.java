package com.renault.analysis.domain.model;

import java.util.List;
import java.util.UUID;

public record StoredHealthReport(UUID telemetryId, HealthReport report, List<Alert> alerts) {}