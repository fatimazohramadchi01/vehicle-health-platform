package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class TirePressureRuleTest {

    private final TirePressureRule rule = new TirePressureRule();

    @Test
    void noPenaltyWhenAllTiresInRange() {
        assertThat(rule.evaluate(context(2.4, 2.4, 2.4, 2.4))).isEmpty();
        assertThat(rule.evaluate(context(2.2, 2.8, 2.4, 2.4))).isEmpty(); // bornes incluses
    }

    @Test
    void minus5PerFaultyTire() {
        assertThat(rule.evaluate(context(2.1, 2.4, 2.4, 2.4))).contains(new Penalty(ScoringRuleType.TIRE_PRESSURE, -5));
        assertThat(rule.evaluate(context(2.4, 2.4, 2.4, 2.9))).contains(new Penalty(ScoringRuleType.TIRE_PRESSURE, -5));
        assertThat(rule.evaluate(context(2.1, 2.9, 2.4, 2.4))).contains(new Penalty(ScoringRuleType.TIRE_PRESSURE, -10));
    }

    @Test
    void cappedAtMinus20() {
        assertThat(rule.evaluate(context(2.0, 2.0, 2.0, 2.0))).contains(new Penalty(ScoringRuleType.TIRE_PRESSURE, -20));
    }

    private AnalysisContext context(double... pressures) {
        List<Double> list = Arrays.stream(pressures).boxed().toList();
        return new AnalysisContext(healthy().withTirePressures(list).build(), List.of());
    }
}