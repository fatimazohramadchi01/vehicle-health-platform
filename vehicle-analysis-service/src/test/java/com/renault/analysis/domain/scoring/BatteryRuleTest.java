package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class BatteryRuleTest {

    private final BatteryRule rule = new BatteryRule();

    @Test
    void noPenaltyWhenBatteryIsHealthy() {
        assertThat(rule.evaluate(context(12.6))).isEmpty();
        assertThat(rule.evaluate(context(12.4))).isEmpty(); // exactement 12,4 V : comparaison stricte
    }

    @Test
    void minus5Below124V() {
        assertThat(rule.evaluate(context(12.3))).contains(new Penalty(ScoringRuleType.BATTERY, -5));
        assertThat(rule.evaluate(context(12.0))).contains(new Penalty(ScoringRuleType.BATTERY, -5));
    }

    @Test
    void minus10Below120V() {
        assertThat(rule.evaluate(context(11.9))).contains(new Penalty(ScoringRuleType.BATTERY, -10));
        assertThat(rule.evaluate(context(11.5))).contains(new Penalty(ScoringRuleType.BATTERY, -10));
    }

    @Test
    void minus15Below115V() {
        assertThat(rule.evaluate(context(11.4))).contains(new Penalty(ScoringRuleType.BATTERY, -15));
    }

    private com.renault.analysis.domain.model.AnalysisContext context(double voltage) {
        return new com.renault.analysis.domain.model.AnalysisContext(
                healthy().withBatteryVoltage(voltage).build(), List.of());
    }
}