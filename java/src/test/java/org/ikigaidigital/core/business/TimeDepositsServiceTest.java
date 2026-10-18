package org.ikigaidigital.core.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.ikigaidigital.infra.inbound.web.TimeDepositDto;
import org.ikigaidigital.infra.inbound.web.WithdrawalDto;
import org.ikigaidigital.infra.outbound.repo.TimeDepositRepository;
import org.ikigaidigital.test.mother.TimeDepositMother;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeDepositsServiceTest {
    private final TimeDepositRepository repository = mock(TimeDepositRepository.class);
    private final TimeDepositsAccrualService batchService = mock(TimeDepositsAccrualService.class);
    private final TimeDepositsService service = new TimeDepositsService(repository, batchService);

    @Test
    void mapsDepositsAndTheirWithdrawalsToDtosInRepositoryOrder() {
        when(repository.findAllWithWithdrawals()).thenReturn(List.of(
                TimeDepositMother.basicWithRecordedWithdrawals(),
                TimeDepositMother.student(9876.54, 365)));

        List<TimeDepositDto> deposits = service.listTimeDeposits();

        assertThat(deposits).containsExactly(
                new TimeDepositDto(1, "basic", new BigDecimal("1234.56"), 90, List.of(
                        new WithdrawalDto(10, new BigDecimal("12.34"), LocalDate.of(2026, 10, 1)),
                        new WithdrawalDto(11, new BigDecimal("5.67"), LocalDate.of(2026, 9, 30)))),
                new TimeDepositDto(2, "student", new BigDecimal("9876.54"), 365, List.of()));
    }

    @Test
    void returnsAnEmptyListWhenNoDepositsExist() {
        when(repository.findAllWithWithdrawals()).thenReturn(List.of());

        List<TimeDepositDto> deposits = service.listTimeDeposits();

        assertThat(deposits).isEmpty();
    }
}
