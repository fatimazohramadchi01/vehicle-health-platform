package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class MileageRuleTest {

    private final MileageRule rule = new MileageRule();

    @Test
    void noPenaltyAtOrBelow100000() {
        assertThat(rule.evaluate(context(0))).isEmpty();
        assertThat(rule.evaluate(context(100_000))).isEmpty();
        assertThat(rule.evaluate(context(124_999))).isEmpty(); // pas de tranche complète
    }

    @Test
    void minus5PerCompleteBracketOf25000() {
        assertThat(rule.evaluate(context(125_000))).contains(new Penalty(ScoringRuleType.MILEAGE, -5));
        assertThat(rule.evaluate(context(149_999))).contains(new Penalty(ScoringRuleType.MILEAGE, -5));
        assertThat(rule.evaluate(context(150_000))).contains(new Penalty(ScoringRuleType.MILEAGE, -10));
    }

    @Test
    void cappedAtMinus20() {
        assertThat(rule.evaluate(context(300_000))).contains(new Penalty(ScoringRuleType.MILEAGE, -20));
    }

    private AnalysisContext context(long mileageKm) {
        return new AnalysisContext(healthy().withMileageKm(mileageKm).build(), List.of());
    }
}