package org.ikigaidigital.infra.inbound.web;

import java.util.List;

import org.ikigaidigital.core.business.TimeDepositsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeDepositController {
    private final TimeDepositsService service;

    public TimeDepositController(TimeDepositsService service) {
        this.service = service;
    }

    @GetMapping(value = "/time-deposits", produces = "application/json")
    public List<TimeDepositDto> listTimeDeposits() {
        return service.listTimeDeposits();
    }

    @PostMapping("/time-deposits/interest-accruals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accrueInterest() {
        service.accrueInterest();
    }
}
