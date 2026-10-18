package org.ikigaidigital.core.domain;

public enum TimeDepositPlan {
    BASIC("basic", 0.01, 31, Integer.MAX_VALUE),
    STUDENT("student", 0.03, 31, 365),
    PREMIUM("premium", 0.05, 46, Integer.MAX_VALUE),
    NONE(null, 0, 0, 0);

    private final String planType;
    private final double annualRate;
    private final int firstEligibleDay;
    private final int lastEligibleDay;

    TimeDepositPlan(String planType, double annualRate,
            int firstEligibleDay, int lastEligibleDay) {
        this.planType = planType;
        this.annualRate = annualRate;
        this.firstEligibleDay = firstEligibleDay;
        this.lastEligibleDay = lastEligibleDay;
    }

    public static TimeDepositPlan forPlan(String planType) {
        for (TimeDepositPlan plan : values()) {
            if (planType.equals(plan.planType)) {
                return plan;
            }
        }
        return NONE;
    }

    public double calculateInterest(double currentBalance, int days) {
        if (this == NONE || days < firstEligibleDay || days > lastEligibleDay) {
            return 0;
        }
        return currentBalance * annualRate / 12;
    }
}
