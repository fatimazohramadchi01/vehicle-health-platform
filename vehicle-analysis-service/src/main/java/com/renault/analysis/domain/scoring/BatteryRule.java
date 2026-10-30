package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class BatteryRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        double voltage = context.snapshot().batteryVoltage();
        int points = 0;
        if (voltage < 11.5) {
            points = -15;
        } else if (voltage < 12.0) {
            points = -10;
        } else if (voltage < 12.4) {
            points = -5;
        }
        return points == 0 ? Optional.empty() : Optional.of(new Penalty(ScoringRuleType.BATTERY, points));
    }
}