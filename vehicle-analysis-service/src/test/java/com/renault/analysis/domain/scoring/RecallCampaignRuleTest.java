package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.RecallCampaign;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class RecallCampaignRuleTest {

    private final RecallCampaignRule rule = new RecallCampaignRule();

    @Test
    void noPenaltyWithoutActiveCampaign() {
        assertThat(rule.evaluate(new AnalysisContext(healthy().build(), List.of()))).isEmpty();
    }

    @Test
    void flatMinus15ForOneOrMoreCampaigns() {
        assertThat(rule.evaluate(context(new RecallCampaign("RC-2026-014", "Contrôle faisceau électrique"))))
                .contains(new Penalty(ScoringRuleType.RECALL_CAMPAIGN, -15));
        assertThat(rule.evaluate(context(
                new RecallCampaign("RC-1", "desc"),
                new RecallCampaign("RC-2", "desc"),
                new RecallCampaign("RC-3", "desc"))))
                .contains(new Penalty(ScoringRuleType.RECALL_CAMPAIGN, -15));
    }

    private AnalysisContext context(RecallCampaign... campaigns) {
        return new AnalysisContext(healthy().build(), List.of(campaigns));
    }
}