package com.renault.analysis.domain.model;

import java.util.List;

public record AnalysisContext(VehicleSnapshot snapshot, List<RecallCampaign> activeRecallCampaigns) {}