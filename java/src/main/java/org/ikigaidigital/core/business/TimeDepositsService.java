package org.ikigaidigital.core.business;

import java.util.List;

import org.ikigaidigital.infra.inbound.web.TimeDepositDto;
import org.ikigaidigital.infra.outbound.repo.TimeDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeDepositsService {
    private static final int ACCRUAL_BATCH_SIZE = 500;

    private final TimeDepositRepository repository;
    private final TimeDepositsAccrualService accrualService;

    public TimeDepositsService(TimeDepositRepository repository,
            TimeDepositsAccrualService accrualService) {
        this.repository = repository;
        this.accrualService = accrualService;
    }

    @Transactional(readOnly = true)
    public List<TimeDepositDto> listTimeDeposits() {
        return repository.findAllWithWithdrawals().stream().map(TimeDepositDto::from).toList();
    }

    public void accrueInterest() {
        // Fix membership at selection time; later inserts belong to the next run.
        List<Integer> ids = repository.findAllIds();
        // Batches commit independently; stop on failure without undoing earlier commits.
        // For now we omit to save state.
        for (int start = 0; start < ids.size(); start += ACCRUAL_BATCH_SIZE) {
            int end = Math.min(start + ACCRUAL_BATCH_SIZE, ids.size());
            accrualService.accrueInterest(ids.subList(start, end));
        }
    }
}
