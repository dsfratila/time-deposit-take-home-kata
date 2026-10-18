package org.ikigaidigital;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.ikigaidigital.domain.TimeDepositBalanceConverter;
import org.ikigaidigital.domain.TimeDepositPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timeDeposits")
public class TimeDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "planType", nullable = false)
    private String planType;

    @Convert(converter = TimeDepositBalanceConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Double balance;

    @Column(nullable = false)
    private int days;

    protected TimeDeposit() {
    }

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
