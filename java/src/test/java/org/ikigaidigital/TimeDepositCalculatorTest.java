package org.ikigaidigital;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TimeDepositCalculatorTest {
    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @ParameterizedTest(name = "{0} at day {1} yields balance {2}")
    @CsvSource({
        "basic, 0, 1200.0",
        "basic, 29, 1200.0",
        "basic, 30, 1200.0",
        "basic, 31, 1201.0",
        "basic, 365, 1201.0",
        "basic, 366, 1201.0",
        "basic, 367, 1201.0",
        "student, 0, 1200.0",
        "student, 29, 1200.0",
        "student, 30, 1200.0",
        "student, 31, 1203.0",
        "student, 365, 1203.0",
        "student, 366, 1200.0",
        "student, 367, 1200.0",
        "premium, 0, 1200.0",
        "premium, 29, 1200.0",
        "premium, 30, 1200.0",
        "premium, 31, 1200.0",
        "premium, 44, 1200.0",
        "premium, 45, 1200.0",
        "premium, 46, 1205.0",
        "premium, 366, 1205.0"
    })
    void appliesMonthlyInterestAtExistingEligibilityBoundaries(
            String planType, int days, double expectedBalance) {
        TimeDeposit deposit = TimeDepositMother.withPlan(planType, 1200.0, days);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(expectedBalance);
    }

    @ParameterizedTest(name = "basic balance {0} becomes {1}")
    @CsvSource({
        "1.0, 1.0",
        "5.99, 5.99",
        "6.0, 6.01",
        "6.01, 6.02",
        "18.0, 18.01",
        "18.01, 18.03",
        "10.123, 10.133",
        "1234567.0, 1235595.81"
    })
    void preservesExistingInterestRounding(double balance, double expectedBalance) {
        TimeDeposit deposit = TimeDepositMother.basic(balance, 31);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(expectedBalance);
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic", "student", "premium"})
    void preservesZeroBalances(String planType) {
        TimeDeposit deposit = TimeDepositMother.withPlan(planType, 0.0, 46);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(0.0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "", "Basic", "Student", "Premium", "BASIC", " basic "})
    void leavesUnrecognizedPlanBalancesUnchanged(String planType) {
        TimeDeposit deposit = TimeDepositMother.withPlan(planType, 1200.0, 46);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(1200.0);
    }

    @Test
    void acceptsAnEmptyList() {
        List<TimeDeposit> deposits = new ArrayList<>();

        calculator.updateBalance(deposits);

        assertThat(deposits).isEmpty();
    }

    @Test
    void updatesMixedDepositsInPlaceAndPreservesTheirOtherFields() {
        TimeDeposit basic = TimeDepositMother.basic(1200.0, 31);
        TimeDeposit student = TimeDepositMother.student(1200.0, 365);
        TimeDeposit premium = TimeDepositMother.premium(1200.0, 46);
        TimeDeposit expiredStudent = TimeDepositMother.student(1200.0, 366);
        List<TimeDeposit> deposits = new ArrayList<>(
                List.of(basic, student, premium, expiredStudent));

        calculator.updateBalance(deposits);

        assertThat(deposits).satisfiesExactly(
                deposit -> assertThat(deposit).isSameAs(basic),
                deposit -> assertThat(deposit).isSameAs(student),
                deposit -> assertThat(deposit).isSameAs(premium),
                deposit -> assertThat(deposit).isSameAs(expiredStudent));
        assertThat(deposits)
                .extracting(TimeDeposit::getId, TimeDeposit::getPlanType,
                        TimeDeposit::getBalance, TimeDeposit::getDays)
                .containsExactly(
                        tuple(1, "basic", 1201.0, 31),
                        tuple(2, "student", 1203.0, 365),
                        tuple(3, "premium", 1205.0, 46),
                        tuple(2, "student", 1200.0, 366));
    }

    @Test
    void appliesInterestToTheUpdatedBalanceOnEveryCall() {
        TimeDeposit deposit = TimeDepositMother.student(1200.0, 365);
        List<TimeDeposit> deposits = List.of(deposit);

        calculator.updateBalance(deposits);
        calculator.updateBalance(deposits);

        assertThat(deposit.getBalance()).isEqualTo(1206.01);
        assertThat(deposit.getDays()).isEqualTo(365);
    }
}
