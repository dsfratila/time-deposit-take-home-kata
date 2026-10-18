package org.ikigaidigital;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeDepositCalculatorTest {
    @Test
    public void updateBalanceTest() {
        TimeDepositCalculator calc = new TimeDepositCalculator();
        List<TimeDeposit> plans = Arrays.asList(
            new TimeDeposit(1, "basic", 1234567.00, 45)
        );
        calc.updateBalance(plans);

        assertThat(1).isEqualTo(1);
    }
}
