package org.ikigaidigital.core.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TimeDepositBalanceConverterTest {
    private final TimeDepositBalanceConverter converter = new TimeDepositBalanceConverter();

    @ParameterizedTest(name = "balance {0} is stored as decimal {1}")
    @CsvSource({
        "0.0, 0.0",
        "0.1, 0.1",
        "1234.56, 1234.56",
        "1234.567, 1234.567",
        "-12.34, -12.34",
        "1000000000000.01, 1000000000000.01"
    })
    void convertsBalanceToDecimalWithoutRounding(double balance, BigDecimal expectedBalance) {
        BigDecimal databaseBalance = converter.convertToDatabaseColumn(balance);

        assertThat(databaseBalance).isEqualByComparingTo(expectedBalance);
    }

    @Test
    void preservesNullWhenConvertingToDatabaseColumn() {
        BigDecimal databaseBalance = converter.convertToDatabaseColumn(null);

        assertThat(databaseBalance).isNull();
    }

    @ParameterizedTest(name = "decimal {0} is loaded as balance {1}")
    @CsvSource({
        "0.00, 0.0",
        "0.10, 0.1",
        "1234.56, 1234.56",
        "1234.567, 1234.567",
        "-12.34, -12.34",
        "1000000000000.01, 1000000000000.01",
        "0.123456789012345678901, 0.12345678901234568"
    })
    void convertsDecimalToDouble(BigDecimal databaseBalance, double expectedBalance) {
        Double balance = converter.convertToEntityAttribute(databaseBalance);

        assertThat(balance).isEqualTo(expectedBalance);
    }

    @Test
    void preservesNullWhenConvertingToEntityAttribute() {
        Double balance = converter.convertToEntityAttribute(null);

        assertThat(balance).isNull();
    }
}
