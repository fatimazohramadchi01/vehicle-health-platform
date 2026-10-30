package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class EngineTemperatureRuleTest {

    private final EngineTemperatureRule rule = new EngineTemperatureRule();

    @Test
    void noPenaltyAtOrBelow105() {
        assertThat(rule.evaluate(context(90))).isEmpty();
        assertThat(rule.evaluate(context(105))).isEmpty();
    }

    @Test
    void tiersWithoutCumulation() {
        assertThat(rule.evaluate(context(106))).contains(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, -5));
        assertThat(rule.evaluate(context(115))).contains(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, -5));
        assertThat(rule.evaluate(context(116))).contains(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, -10));
        assertThat(rule.evaluate(context(119.9))).contains(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, -10));
        assertThat(rule.evaluate(context(120))).contains(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, -15));
    }

    private AnalysisContext context(double temperature) {
        return new AnalysisContext(healthy().withEngineTemperatureC(temperature).build(), List.of());
    }
}