package org.ikigaidigital.infra.outbound.repo;

import java.util.List;

import org.ikigaidigital.core.domain.TimeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimeDepositRepository extends JpaRepository<TimeDeposit, Integer> {
    @Query("SELECT deposit FROM TimeDeposit deposit "
            + "LEFT JOIN FETCH deposit.withdrawals ORDER BY deposit.id")
    List<TimeDeposit> findAllWithWithdrawals();
}
