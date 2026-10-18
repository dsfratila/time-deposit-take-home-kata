package org.ikigaidigital.test.util;

import java.util.Arrays;

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
}
