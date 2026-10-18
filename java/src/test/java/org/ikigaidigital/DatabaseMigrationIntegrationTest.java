package org.ikigaidigital;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class DatabaseMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesDepositsAndWithdrawalsWithGeneratedIntegerIds() {
        var balance = new BigDecimal("1234.56");
        var amount = new BigDecimal("34.56");
        var date = LocalDate.of(2026, 10, 18);

        var depositId = jdbc.queryForObject("""
                INSERT INTO "timeDeposits" ("planType", days, balance)
                VALUES ('BASIC', 60, ?) RETURNING id
                """, Integer.class, balance);
        var withdrawalId = jdbc.queryForObject("""
                INSERT INTO withdrawals ("timeDepositId", amount, date)
                VALUES (?, ?, ?) RETURNING id
                """, Integer.class, depositId, amount, date);

        assertThat(depositId).isPositive();
        assertThat(withdrawalId).isPositive();
        assertThat(jdbc.queryForObject("""
                SELECT balance FROM "timeDeposits" WHERE id = ?
                """, BigDecimal.class, depositId)).isEqualByComparingTo(balance);
        assertThat(jdbc.queryForMap("""
                SELECT "timeDepositId", amount, date FROM withdrawals WHERE id = ?
                """, withdrawalId))
                .containsEntry("timeDepositId", depositId)
                .containsEntry("amount", amount)
                .containsEntry("date", java.sql.Date.valueOf(date));
    }

    @Test
    void rejectsWithdrawalsWithoutAnExistingDeposit() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO withdrawals ("timeDepositId", amount, date)
                VALUES (-1, 10.00, CURRENT_DATE)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void requiresEveryDepositAndWithdrawalField() {
        var nullableColumns = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_schema = 'public'
                    AND table_name IN ('timeDeposits', 'withdrawals')
                    AND is_nullable = 'YES'
                """, String.class);

        assertThat(nullableColumns).isEmpty();
    }
}
