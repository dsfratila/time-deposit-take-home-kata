package org.ikigaidigital.infra.outbound.repo;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.ikigaidigital.core.domain.TimeDeposit;
import org.ikigaidigital.test.base.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class TimeDepositRepositoryIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TimeDepositRepository repository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void fetchesDepositsAndTheirWithdrawalsInOneQuery() {
        database.insertData("fixtures/time-deposits/with-withdrawals.sql");
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<TimeDeposit> deposits = repository.findAllWithWithdrawals();

        assertThat(deposits).extracting(deposit -> deposit.getWithdrawals().size())
                .containsExactly(2, 0, 1);
        assertThat(deposits).allSatisfy(deposit ->
                assertThat(deposit.getWithdrawals()).allSatisfy(withdrawal ->
                        assertThat(withdrawal.getTimeDeposit()).isSameAs(deposit)));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
