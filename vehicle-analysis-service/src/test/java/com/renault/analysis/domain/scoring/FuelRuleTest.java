package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class FuelRuleTest {

    private final FuelRule rule = new FuelRule();

    @Test
    void noPenaltyAtOrAbove10Percent() {
        assertThat(rule.evaluate(context(60))).isEmpty();
        assertThat(rule.evaluate(context(10))).isEmpty();
    }

    @Test
    void tiersWithoutCumulation() {
        assertThat(rule.evaluate(context(9))).contains(new Penalty(ScoringRuleType.FUEL, -2));
        assertThat(rule.evaluate(context(5))).contains(new Penalty(ScoringRuleType.FUEL, -2));
        assertThat(rule.evaluate(context(4))).contains(new Penalty(ScoringRuleType.FUEL, -5));
    }

    private AnalysisContext context(int fuelLevelPercent) {
        return new AnalysisContext(healthy().withFuelLevelPercent(fuelLevelPercent).build(), List.of());
    }
}