package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class RecallCampaignRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        if (context.activeRecallCampaigns().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Penalty(ScoringRuleType.RECALL_CAMPAIGN, -15));
    }
}