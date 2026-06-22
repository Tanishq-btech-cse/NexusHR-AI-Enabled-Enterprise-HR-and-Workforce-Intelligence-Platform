package com.nexushr.attendance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class AttendanceServiceTests {
    @Autowired
    AttendanceService service;
    @Autowired
    LeaveBalanceRepository balances;

    @Test
    void approvedLeaveConsumesBalance() {
        UUID employeeId = UUID.randomUUID();
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployeeId(employeeId);
        balance.setLeaveType("ANNUAL");
        balance.setOpeningBalance(BigDecimal.TEN);
        balances.save(balance);

        LeaveRequest request = service.requestLeave(employeeId, "ANNUAL", LocalDate.now(), LocalDate.now().plusDays(1), "Vacation");
        service.decideLeave(request.getId(), LeaveStatus.APPROVED, UUID.randomUUID());

        assertThat(balances.findByEmployeeIdAndLeaveType(employeeId, "ANNUAL").orElseThrow().available())
                .isEqualByComparingTo("8.00");
    }
}
