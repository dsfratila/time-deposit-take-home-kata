package org.ikigaidigital.infra.inbound.web;

import java.util.List;

import org.ikigaidigital.core.business.TimeDepositsService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
