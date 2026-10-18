package org.ikigaidigital;

final class TimeDepositMother {
    private TimeDepositMother() {
    }

    static TimeDeposit basic(double balance, int days) {
        return new TimeDeposit(1, "basic", balance, days);
    }

    static TimeDeposit student(double balance, int days) {
        return new TimeDeposit(2, "student", balance, days);
    }

    static TimeDeposit premium(double balance, int days) {
        return new TimeDeposit(3, "premium", balance, days);
    }

    static TimeDeposit withPlan(String planType, double balance, int days) {
        return new TimeDeposit(1, planType, balance, days);
    }
}
