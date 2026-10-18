package org.ikigaidigital;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TimeDeposit {
    private int id;
    private String planType;
    private Double balance;
    private int days;

    public TimeDeposit(int id, String planType, Double balance, int days) {
        this.id = id;
        this.planType = planType;
        this.balance = balance;
        this.days = days;
    }

    public int getId() { return id; }

    public String getPlanType() {
        return planType;
    }

    public Double getBalance() {
        return balance;
    }

    public int getDays() {
        return days;
    }

    public void setBalance(Double newBalance) {
        this.balance = newBalance;
    }

    public void accrueMonthlyInterest() {
        double interest = 0;
        if (days > 30) {
            interest = TimeDepositPlan.forPlan(planType).calculateInterest(balance, days);
        }

        // Preserve legacy floating-point arithmetic and round only the interest, not the balance.
        double roundedInterest = new BigDecimal(interest)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        setBalance(balance + roundedInterest);
    }
}
