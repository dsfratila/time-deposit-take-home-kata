package org.ikigaidigital.core.domain;

import org.ikigaidigital.test.mother.TimeDepositMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TimeDepositTest {
    @Test
    void exposesTheValuesProvidedToTheConstructor() {
        TimeDeposit deposit = new TimeDeposit(42, "student", 1234.567, 365);

        assertThat(deposit.getId()).isEqualTo(42);
        assertThat(deposit.getPlanType()).isEqualTo("student");
        assertThat(deposit.getBalance()).isEqualTo(1234.567);
        assertThat(deposit.getDays()).isEqualTo(365);
    }

    @Test
    void replacesTheBalanceWithoutChangingOtherFields() {
        TimeDeposit deposit = TimeDepositMother.premium(1200.0, 46);

        deposit.setBalance(2345.678);

        assertThat(deposit.getBalance()).isEqualTo(2345.678);
        assertThat(deposit.getId()).isEqualTo(3);
        assertThat(deposit.getPlanType()).isEqualTo("premium");
        assertThat(deposit.getDays()).isEqualTo(46);
    }

    @Test
    void addsInterestFromTheSelectedPlanToTheBalance() {
        TimeDeposit deposit = TimeDepositMother.premium(1200.0, 46);

        deposit.accrueMonthlyInterest();

        assertThat(deposit.getBalance()).isEqualTo(1205.0);
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

        deposit.accrueMonthlyInterest();

        assertThat(deposit.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    void accruesInterestOnTheUpdatedBalanceWithoutChangingOtherFields() {
        TimeDeposit deposit = TimeDepositMother.student(1200.0, 365);

        deposit.accrueMonthlyInterest();
        deposit.accrueMonthlyInterest();

        assertThat(deposit.getBalance()).isEqualTo(1206.01);
        assertThat(deposit.getId()).isEqualTo(2);
        assertThat(deposit.getPlanType()).isEqualTo("student");
        assertThat(deposit.getDays()).isEqualTo(365);
    }
}
