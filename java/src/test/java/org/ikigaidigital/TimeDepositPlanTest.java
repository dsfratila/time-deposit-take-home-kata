package org.ikigaidigital;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TimeDepositPlanTest {
    @ParameterizedTest(name = "plan name {0} resolves to {1}")
    @CsvSource({
        "basic, BASIC",
        "student, STUDENT",
        "premium, PREMIUM"
    })
    void resolvesRecognizedPlanNames(String planType, TimeDepositPlan expectedPlan) {
        TimeDepositPlan plan = TimeDepositPlan.forPlan(planType);

        assertThat(plan).isSameAs(expectedPlan);
    }

    @ParameterizedTest(name = "unrecognized plan name [{0}] resolves to NONE")
    @ValueSource(strings = {"unknown", "", "Basic", "BASIC", " basic "})
    void fallsBackForUnrecognizedPlanNames(String planType) {
        TimeDepositPlan plan = TimeDepositPlan.forPlan(planType);

        assertThat(plan).isSameAs(TimeDepositPlan.NONE);
    }

    @ParameterizedTest(name = "{0} at day {1} yields monthly interest {2}")
    @CsvSource({
        "BASIC, 30, 0.0",
        "BASIC, 31, 1.0",
        "BASIC, 2147483647, 1.0",
        "STUDENT, 30, 0.0",
        "STUDENT, 31, 3.0",
        "STUDENT, 365, 3.0",
        "STUDENT, 366, 0.0",
        "PREMIUM, 45, 0.0",
        "PREMIUM, 46, 5.0",
        "PREMIUM, 2147483647, 5.0"
    })
    void calculatesMonthlyInterestAtEligibilityBoundaries(
            TimeDepositPlan plan, int days, double expectedInterest) {
        double balance = 1200.0;

        double interest = plan.calculateInterest(balance, days);

        assertThat(interest).isEqualTo(expectedInterest);
    }

    @ParameterizedTest(name = "NONE yields no interest at day {0}")
    @ValueSource(ints = {0, 46})
    void fallbackNeverAccruesInterest(int days) {
        double balance = 1200.0;

        double interest = TimeDepositPlan.NONE.calculateInterest(balance, days);

        assertThat(interest).isZero();
    }

    @ParameterizedTest(name = "{0} yields no interest on a zero balance")
    @EnumSource(value = TimeDepositPlan.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void calculatesZeroInterestForZeroBalances(TimeDepositPlan plan) {
        double balance = 0.0;

        double interest = plan.calculateInterest(balance, 46);

        assertThat(interest).isZero();
    }

    @ParameterizedTest(name = "{0} leaves interest {1} unrounded")
    @CsvSource({
        "BASIC, 0.005",
        "STUDENT, 0.015",
        "PREMIUM, 0.025"
    })
    void returnsInterestWithoutMonetaryRounding(TimeDepositPlan plan, double expectedInterest) {
        double balance = 6.0;

        double interest = plan.calculateInterest(balance, 46);

        assertThat(interest).isCloseTo(expectedInterest, within(1.0e-15));
    }
}
