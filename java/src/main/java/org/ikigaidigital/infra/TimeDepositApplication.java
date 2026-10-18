package org.ikigaidigital.infra;

import org.ikigaidigital.TimeDeposit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackageClasses = TimeDeposit.class)
@EntityScan(basePackageClasses = TimeDeposit.class)
public class TimeDepositApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeDepositApplication.class, args);
    }
}
