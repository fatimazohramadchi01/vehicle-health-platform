package com.renault.analysis.domain.model;

import java.time.Instant;
import java.util.List;

public record HealthReport(String vin, Instant measuredAt, int score, List<Penalty> penalties) {}