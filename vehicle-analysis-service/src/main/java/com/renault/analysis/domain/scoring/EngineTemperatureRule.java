package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class EngineTemperatureRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        double temperature = context.snapshot().engineTemperatureC();
        int points = 0;
        if (temperature >= 120) {
            points = -15;
        } else if (temperature > 115) {
            points = -10;
        } else if (temperature > 105) {
            points = -5;
        }
        return points == 0
                ? Optional.empty()
                : Optional.of(new Penalty(ScoringRuleType.ENGINE_TEMPERATURE, points));
    }
}