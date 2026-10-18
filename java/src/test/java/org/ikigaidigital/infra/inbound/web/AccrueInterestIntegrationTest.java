package org.ikigaidigital.infra.inbound.web;

import java.math.BigDecimal;
import java.util.List;

import org.ikigaidigital.test.base.AbstractIntegrationTest;
import org.ikigaidigital.test.util.DatabaseHelper.StoredTimeDeposit;
import org.ikigaidigital.test.util.DatabaseHelper.StoredWithdrawal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.test.util.StringUtils.readJson;
import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class AccrueInterestIntegrationTest extends AbstractIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void accruesInterestAndPreservesDaysAndWithdrawals() {
        database.insertData("fixtures/time-deposits/interest-accrual.sql");
        final List<StoredWithdrawal> withdrawals = database.readWithdrawals();

        requestAccrual();

        assertDeposits("after-first-run.json");
        assertThat(database.readDeposits()).extracting(StoredTimeDeposit::balance)
                .containsExactly(new BigDecimal("1201.00"), new BigDecimal("1203.00"),
                        new BigDecimal("1205.00"), new BigDecimal("1200.00"),
                        new BigDecimal("1200.00"), new BigDecimal("1200.00"));
        assertThat(database.readDeposits()).extracting(StoredTimeDeposit::days)
                .containsExactly(31, 365, 46, 30, 366, 90);
        assertThat(database.readWithdrawals()).isEqualTo(withdrawals);
    }

    @Test
    void repeatedRequestsAccrueAgainOnCommittedBalances() {
        database.insertData("fixtures/time-deposits/interest-accrual.sql");

        requestAccrual();
        requestAccrual();

        assertDeposits("after-second-run.json");
        assertThat(database.readDeposits()).extracting(StoredTimeDeposit::balance)
                .containsExactly(new BigDecimal("1202.00"), new BigDecimal("1206.01"),
                        new BigDecimal("1210.02"), new BigDecimal("1200.00"),
                        new BigDecimal("1200.00"), new BigDecimal("1200.00"));
    }

    @Test
    void succeedsWhenNoDepositsExist() {
        requestAccrual();

        assertThat(database.readDeposits()).isEmpty();
        assertThat(database.readWithdrawals()).isEmpty();
    }

    @Test
    void processesTwoFullBatchesAndTheFinalPartialBatch() {
        database.insertData("fixtures/time-deposits/multiple-batches.sql");
        final List<StoredWithdrawal> withdrawals = database.readWithdrawals();

        requestAccrual();

        List<StoredTimeDeposit> deposits = database.readDeposits();
        assertThat(deposits).hasSize(1001);
        assertThat(deposits).extracting(StoredTimeDeposit::balance)
                .containsOnly(new BigDecimal("1203.00"));
        assertThat(deposits).extracting(StoredTimeDeposit::days).containsOnly(365);
        assertThat(database.readWithdrawals()).isEqualTo(withdrawals);
    }

    @Test
    void keepsEarlierBatchesAndSkipsLaterBatchesWhenOneBatchFails() {
        database.insertData("fixtures/time-deposits/multiple-batches.sql",
                "fixtures/time-deposits/failing-second-batch.sql");
        final List<StoredTimeDeposit> before = database.readDeposits();
        final List<StoredWithdrawal> withdrawals = database.readWithdrawals();

        given().port(port)
                .when().post("/time-deposits/interest-accruals")
                .then().statusCode(500);

        List<StoredTimeDeposit> after = database.readDeposits();
        assertThat(after).hasSize(1001);
        assertThat(after.subList(0, 500)).extracting(StoredTimeDeposit::balance)
                .containsOnly(new BigDecimal("1203.00"));
        assertThat(after.subList(500, 1001)).isEqualTo(before.subList(500, 1001));
        assertThat(database.readWithdrawals()).isEqualTo(withdrawals);
    }

    private void requestAccrual() {
        Response response = given().port(port)
                .when().post("/time-deposits/interest-accruals")
                .then().statusCode(204).extract().response();
        assertThat(response.asString()).isEmpty();
    }

    private void assertDeposits(String responseFile) {
        Response response = given().port(port).accept("application/json")
                .when().get("/time-deposits")
                .then().statusCode(200).contentType("application/json")
                .extract().response();
        assertThatJson(response.asString())
                .isEqualTo(readJson("responses/accrue-interest/" + responseFile));
    }
}
