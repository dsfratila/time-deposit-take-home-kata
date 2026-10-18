package org.ikigaidigital.infra.inbound.web;

import java.math.BigDecimal;
import java.util.List;

import org.ikigaidigital.core.domain.TimeDeposit;

public record TimeDepositDto(int id, String planType, BigDecimal balance, int days,
                             List<WithdrawalDto> withdrawals) {
    public static TimeDepositDto from(TimeDeposit deposit) {
        return new TimeDepositDto(deposit.getId(), deposit.getPlanType(),
                BigDecimal.valueOf(deposit.getBalance()), deposit.getDays(),
                deposit.getWithdrawals().stream().map(WithdrawalDto::from).toList());
    }
}
