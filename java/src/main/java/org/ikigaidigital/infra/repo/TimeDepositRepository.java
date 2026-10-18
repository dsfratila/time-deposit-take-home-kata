package org.ikigaidigital.infra.repo;

import org.ikigaidigital.TimeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeDepositRepository extends JpaRepository<TimeDeposit, Integer> {
}
