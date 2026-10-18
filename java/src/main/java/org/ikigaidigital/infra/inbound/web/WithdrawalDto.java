package org.ikigaidigital.infra.inbound.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ikigaidigital.core.domain.Withdrawal;

public record WithdrawalDto(int id, BigDecimal amount, LocalDate date) {
    static WithdrawalDto from(Withdrawal withdrawal) {
        return new WithdrawalDto(withdrawal.getId(), withdrawal.getAmount(),
                withdrawal.getDate());
    }
}
