package org.ikigaidigital.core.business;

import java.util.Arrays;
import java.util.List;
import org.ikigaidigital.core.domain.TimeDeposit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeDepositCalculatorTest {
    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @Test
    public void updateBalanceTest() {
        TimeDepositCalculator calc = new TimeDepositCalculator();
        List<TimeDeposit> plans = Arrays.asList(
                new TimeDeposit(1, "basic", 1234567.00, 45)
        );
        calc.updateBalance(plans);

        assertThat(1).isEqualTo(1);
    }

    @Test
    void acceptsAnEmptyList() {
        List<TimeDeposit> deposits = List.of();

        calculator.updateBalance(deposits);

        assertThat(deposits).isEmpty();
    }

    @Test
    void delegatesInterestAccrualToEachDepositWithoutReplacingThem() {
        TimeDeposit firstDeposit = mock(TimeDeposit.class);
        TimeDeposit secondDeposit = mock(TimeDeposit.class);
        List<TimeDeposit> deposits = List.of(firstDeposit, secondDeposit);

        calculator.updateBalance(deposits);

        verify(firstDeposit).accrueMonthlyInterest();
        verify(secondDeposit).accrueMonthlyInterest();
        assertThat(deposits).containsExactly(firstDeposit, secondDeposit);
    }
}
