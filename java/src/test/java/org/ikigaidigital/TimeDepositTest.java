package org.ikigaidigital;

import org.junit.jupiter.api.Test;

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
}
