package com.renault.analysis.domain.ports;

import com.renault.analysis.domain.model.RecallCampaign;

import java.util.List;

public interface RecallProvider {
    List<RecallCampaign> activeCampaignsFor(String vin);
}