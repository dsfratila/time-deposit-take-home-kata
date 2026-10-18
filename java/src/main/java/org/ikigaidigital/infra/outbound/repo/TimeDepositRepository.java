package org.ikigaidigital.infra.outbound.repo;

import java.util.List;

import org.ikigaidigital.core.domain.TimeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TimeDepositRepository extends JpaRepository<TimeDeposit, Integer> {
    @Query("SELECT deposit FROM TimeDeposit deposit "
            + "LEFT JOIN FETCH deposit.withdrawals ORDER BY deposit.id")
    List<TimeDeposit> findAllWithWithdrawals();

    @Query("SELECT deposit.id FROM TimeDeposit deposit ORDER BY deposit.id")
    List<Integer> findAllIds();

    // Consistent lock order reduces deadlocks between overlapping accrual runs.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT deposit FROM TimeDeposit deposit WHERE deposit.id IN :ids ORDER BY deposit.id")
    List<TimeDeposit> findAllByIdForUpdate(@Param("ids") List<Integer> ids);
}
