package org.ikigaidigital;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeDepositCalculatorTest {
    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

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
