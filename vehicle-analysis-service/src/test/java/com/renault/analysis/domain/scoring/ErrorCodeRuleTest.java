package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.renault.analysis.domain.scoring.Snapshots.healthy;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeRuleTest {

    private final ErrorCodeRule rule = new ErrorCodeRule();

    @Test
    void noPenaltyWhenNoErrorCodes() {
        assertThat(rule.evaluate(context())).isEmpty();
    }

    @Test
    void duplicatedCodesCountOnce() {
        assertThat(rule.evaluate(context("P0301", "P0301"))).contains(new Penalty(ScoringRuleType.OBD_ERROR, -5));
    }

    @Test
    void minus5PerUniqueCode() {
        assertThat(rule.evaluate(context("P0301", "P0171"))).contains(new Penalty(ScoringRuleType.OBD_ERROR, -10));
    }

    @Test
    void cappedAtMinus20() {
        assertThat(rule.evaluate(context("P0301", "P0171", "P0420", "P0128", "P0455")))
                .contains(new Penalty(ScoringRuleType.OBD_ERROR, -20));
    }

    private AnalysisContext context(String... codes) {
        return new AnalysisContext(healthy().withErrorCodes(List.of(codes)).build(), List.of());
    }
}