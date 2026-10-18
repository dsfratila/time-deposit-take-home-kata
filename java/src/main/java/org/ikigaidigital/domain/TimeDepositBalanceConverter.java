package org.ikigaidigital.domain;

import java.math.BigDecimal;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TimeDepositBalanceConverter implements AttributeConverter<Double, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Double balance) {
        return balance == null ? null : BigDecimal.valueOf(balance);
    }

    @Override
    public Double convertToEntityAttribute(BigDecimal balance) {
        return balance == null ? null : balance.doubleValue();
    }
}
