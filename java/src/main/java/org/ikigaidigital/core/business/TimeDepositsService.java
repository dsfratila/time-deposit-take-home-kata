package org.ikigaidigital.core.business;

import java.util.List;

import org.ikigaidigital.infra.inbound.web.TimeDepositDto;
import org.ikigaidigital.infra.outbound.repo.TimeDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeDepositsService {
    private final TimeDepositRepository repository;

    public TimeDepositsService(TimeDepositRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TimeDepositDto> listTimeDeposits() {
        return repository.findAllWithWithdrawals().stream().map(TimeDepositDto::from).toList();
    }
}
