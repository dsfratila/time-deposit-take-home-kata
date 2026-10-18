package org.ikigaidigital;

import java.math.BigDecimal;
import org.ikigaidigital.infra.repo.TimeDepositRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class TimeDepositRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TimeDepositRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void persistsAndRetrievesDepositWithGeneratedIntegerIdAndDecimalBalance() {
        var deposit = new TimeDeposit(0, "basic", 1234.56, 60);

        var saved = repository.saveAndFlush(deposit);
        entityManager.clear();
        var loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getId()).isPositive();
        assertThat(loaded.getPlanType()).isEqualTo("basic");
        assertThat(loaded.getBalance()).isEqualTo(1234.56);
        assertThat(loaded.getDays()).isEqualTo(60);
        assertThat(jdbc.queryForObject("""
                SELECT balance FROM "timeDeposits" WHERE id = ?
                """, BigDecimal.class, saved.getId())).isEqualByComparingTo("1234.56");
    }

    @Test
    void persistsBalanceChangesThroughTheExistingSetter() {
        var deposit = repository.saveAndFlush(new TimeDeposit(0, "student", 1200.0, 365));

        deposit.setBalance(1203.0);
        repository.flush();
        entityManager.clear();

        assertThat(repository.findById(deposit.getId()).orElseThrow().getBalance())
                .isEqualTo(1203.0);
        assertThat(jdbc.queryForObject("""
                SELECT balance FROM "timeDeposits" WHERE id = ?
                """, BigDecimal.class, deposit.getId())).isEqualByComparingTo("1203.00");
    }
}
