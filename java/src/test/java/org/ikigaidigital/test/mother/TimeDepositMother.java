package org.ikigaidigital.test.mother;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.ikigaidigital.core.domain.TimeDeposit;
import org.ikigaidigital.core.domain.Withdrawal;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

public final class TimeDepositMother {
    private TimeDepositMother() {
    }

    public static TimeDeposit basic(double balance, int days) {
        return new TimeDeposit(1, "basic", balance, days);
    }

    public static TimeDeposit basicWithRecordedWithdrawals() {
        TimeDeposit deposit = basic(1234.56, 90);
        ReflectionTestUtils.setField(deposit, "withdrawals", List.of(
                recordedWithdrawal(10, deposit, "12.34", LocalDate.of(2026, 10, 1)),
                recordedWithdrawal(11, deposit, "5.67", LocalDate.of(2026, 9, 30))));
        return deposit;
    }

    public static TimeDeposit student(double balance, int days) {
        return new TimeDeposit(2, "student", balance, days);
    }

    public static TimeDeposit premium(double balance, int days) {
        return new TimeDeposit(3, "premium", balance, days);
    }

    public static TimeDeposit withPlan(String planType, double balance, int days) {
        return new TimeDeposit(1, planType, balance, days);
    }

    private static Withdrawal recordedWithdrawal(int id, TimeDeposit deposit,
            String amount, LocalDate date) {
        Withdrawal withdrawal = BeanUtils.instantiateClass(Withdrawal.class);
        ReflectionTestUtils.setField(withdrawal, "id", id);
        ReflectionTestUtils.setField(withdrawal, "timeDeposit", deposit);
        ReflectionTestUtils.setField(withdrawal, "amount", new BigDecimal(amount));
        ReflectionTestUtils.setField(withdrawal, "date", date);
        return withdrawal;
    }
}
