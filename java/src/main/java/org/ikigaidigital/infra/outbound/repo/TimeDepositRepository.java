package org.ikigaidigital.infra.outbound.repo;

import org.ikigaidigital.core.domain.TimeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeDepositRepository extends JpaRepository<TimeDeposit, Integer> {
}
