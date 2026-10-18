package org.ikigaidigital.infra.inbound.web;

import org.ikigaidigital.test.base.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.response.Response;

import static org.ikigaidigital.test.util.StringUtils.readJson;
import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class ListTimeDepositsIntegrationTest extends AbstractIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void returnsAllDepositsWithTheirWithdrawals() {
        database.insertData("fixtures/time-deposits/with-withdrawals.sql");

        Response response = requestDeposits();

        assertThatJson(response.asString())
                .isEqualTo(readJson("responses/list-time-deposits/populated.json"));
    }

    @Test
    void returnsAnEmptyArrayWhenNoDepositsExist() {
        Response response = requestDeposits();

        assertThatJson(response.asString())
                .isEqualTo(readJson("responses/list-time-deposits/empty.json"));
    }

    private Response requestDeposits() {
        return given().port(port).accept("application/json")
                .when().get("/time-deposits")
                .then().statusCode(200).contentType("application/json")
                .extract().response();
    }
}
