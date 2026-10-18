package org.ikigaidigital.test.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

public class DatabaseHelper {
    private final JdbcTemplate jdbc;

    public DatabaseHelper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void insertData(String... resourcePaths) {
        ClassPathResource[] scripts = Arrays.stream(resourcePaths)
                .map(ClassPathResource::new).toArray(ClassPathResource[]::new);
        new ResourceDatabasePopulator(scripts).execute(jdbc.getDataSource());
    }

    @Transactional
    public void cleanUp() {
        jdbc.update("DELETE FROM \"withdrawals\"");
        jdbc.update("DELETE FROM \"timeDeposits\"");
    }

    @Transactional
    public void resetDatabase() {
        jdbc.execute("TRUNCATE TABLE \"withdrawals\", \"timeDeposits\" RESTART IDENTITY");
    }

    public List<StoredTimeDeposit> readDeposits() {
        return jdbc.query("SELECT * FROM \"timeDeposits\" ORDER BY id", (row, index) ->
                new StoredTimeDeposit(row.getInt("id"), row.getString("planType"),
                        row.getBigDecimal("balance"), row.getInt("days")));
    }

    public List<StoredWithdrawal> readWithdrawals() {
        return jdbc.query("SELECT * FROM \"withdrawals\" ORDER BY id", (row, index) ->
                new StoredWithdrawal(row.getInt("id"), row.getInt("timeDepositId"),
                        row.getBigDecimal("amount"), row.getObject("date", LocalDate.class)));
    }

    public int currentBackendPid() {
        return jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class);
    }

    public boolean hasTransactionBlockedBy(int backendPid) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_locks "
                        + "WHERE NOT granted AND ? = ANY(pg_blocking_pids(pid)))",
                Boolean.class, backendPid));
    }

    public record StoredTimeDeposit(int id, String planType, BigDecimal balance, int days) { }

    public record StoredWithdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) { }
}
