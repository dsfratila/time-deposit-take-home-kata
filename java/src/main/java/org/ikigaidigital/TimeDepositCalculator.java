package org.ikigaidigital;

import java.util.List;

public class TimeDepositCalculator {
    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            deposit.accrueMonthlyInterest();
        }
    }
}
