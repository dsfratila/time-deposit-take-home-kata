package org.ikigaidigital.core.business;

import java.util.List;

import org.ikigaidigital.infra.outbound.repo.TimeDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeDepositsAccrualService {
    private final TimeDepositRepository repository;
    private final TimeDepositCalculator calculator;

    public TimeDepositsAccrualService(TimeDepositRepository repository,
            TimeDepositCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accrueInterest(List<Integer> ids) {
        if (ids.isEmpty()) {
            return;
        }

        // Retries accrue again, including deposits committed before a previous run failed.
        calculator.updateBalance(repository.findAllByIdForUpdate(ids));
    }
}
