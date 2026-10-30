package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class FuelRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        int fuelLevelPercent = context.snapshot().fuelLevelPercent();
        int points = 0;
        if (fuelLevelPercent < 5) {
            points = -5;
        } else if (fuelLevelPercent < 10) {
            points = -2;
        }
        return points == 0
                ? Optional.empty()
                : Optional.of(new Penalty(ScoringRuleType.FUEL, points));
    }
}