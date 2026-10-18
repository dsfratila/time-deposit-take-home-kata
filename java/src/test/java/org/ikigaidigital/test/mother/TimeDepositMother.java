package org.ikigaidigital.test.mother;

import org.ikigaidigital.core.domain.TimeDeposit;

public final class TimeDepositMother {
    private TimeDepositMother() {
    }

    public static TimeDeposit basic(double balance, int days) {
        return new TimeDeposit(1, "basic", balance, days);
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
}
