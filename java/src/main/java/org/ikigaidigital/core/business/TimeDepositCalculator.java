package org.ikigaidigital.core.business;

import java.util.List;
import org.ikigaidigital.core.domain.TimeDeposit;
import org.springframework.stereotype.Component;

@Component
public class TimeDepositCalculator {
    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            deposit.accrueMonthlyInterest();
        }
    }
}
