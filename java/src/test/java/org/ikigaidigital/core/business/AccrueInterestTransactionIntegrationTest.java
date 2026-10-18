package org.ikigaidigital.core.business;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.ikigaidigital.infra.outbound.repo.TimeDepositRepository;
import org.ikigaidigital.test.base.AbstractIntegrationTest;
import org.ikigaidigital.test.util.DatabaseHelper.StoredTimeDeposit;
import org.ikigaidigital.test.util.DatabaseHelper.StoredWithdrawal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class AccrueInterestTransactionIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TimeDepositsAccrualService service;

    @Autowired
    private TimeDepositRepository repository;

    @Autowired
    private TimeDepositCalculator calculator;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void rollsBackEveryBalanceInTheBatchWhenOneUpdateExceedsTheColumnPrecision() {
        database.insertData("fixtures/time-deposits/overflow-on-accrual.sql");
        final List<StoredTimeDeposit> deposits = database.readDeposits();
        final List<StoredWithdrawal> withdrawals = database.readWithdrawals();

        assertThatThrownBy(() -> service.accrueInterest(List.of(1, 2)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(database.readDeposits()).isEqualTo(deposits);
        assertThat(database.readWithdrawals()).isEqualTo(withdrawals);
    }

    @Test
    void concurrentBatchesWaitForCommitAndAccrueOnTheCommittedBalances() throws Exception {
        database.insertData("fixtures/time-deposits/interest-accrual.sql");
        final List<Integer> ids = repository.findAllIds();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> secondRun = transaction.execute(status -> {
                calculator.updateBalance(repository.findAllByIdForUpdate(ids));
                int backendPid = database.currentBackendPid();
                Future<?> pendingRun = executor.submit(() -> service.accrueInterest(ids));
                await().atMost(Duration.ofSeconds(10))
                        .until(() -> database.hasTransactionBlockedBy(backendPid));
                assertThat(pendingRun).isNotDone();
                return pendingRun;
            });
            secondRun.get(10, TimeUnit.SECONDS);
        }

        assertThat(database.readDeposits()).extracting(StoredTimeDeposit::balance)
                .containsExactly(new BigDecimal("1202.00"), new BigDecimal("1206.01"),
                        new BigDecimal("1210.02"), new BigDecimal("1200.00"),
                        new BigDecimal("1200.00"), new BigDecimal("1200.00"));
    }

    @Test
    void commitsOnlyTheSelectedBatchEvenWhenTheCallingTransactionRollsBack() {
        database.insertData("fixtures/time-deposits/interest-accrual.sql");
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status -> {
            service.accrueInterest(List.of(2));
            status.setRollbackOnly();
        });

        assertThat(database.readDeposits()).extracting(StoredTimeDeposit::balance)
                .containsExactly(new BigDecimal("1200.00"), new BigDecimal("1203.00"),
                        new BigDecimal("1200.00"), new BigDecimal("1200.00"),
                        new BigDecimal("1200.00"), new BigDecimal("1200.00"));
    }
}
