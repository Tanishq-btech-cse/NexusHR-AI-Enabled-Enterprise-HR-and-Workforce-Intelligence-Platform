package com.nexushr.dashboard;

import com.nexushr.attendance.LeaveRequestRepository;
import com.nexushr.attendance.LeaveStatus;
import com.nexushr.employee.ApprovalStatus;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.employee.EmployeeStatus;
import com.nexushr.employee.WorkflowStepRepository;
import com.nexushr.notification.NotificationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final EmployeeRepository employees;
    private final WorkflowStepRepository workflowSteps;
    private final LeaveRequestRepository leaveRequests;
    private final NotificationService notifications;

    public DashboardController(EmployeeRepository employees, WorkflowStepRepository workflowSteps,
                               LeaveRequestRepository leaveRequests, NotificationService notifications) {
        this.employees = employees;
        this.workflowSteps = workflowSteps;
        this.leaveRequests = leaveRequests;
        this.notifications = notifications;
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    Map<String, Object> metrics() {
        return Map.of(
                "totalEmployees", employees.count(),
                "activeEmployees", employees.countByStatus(EmployeeStatus.ACTIVE),
                "onboardingEmployees", employees.countByStatus(EmployeeStatus.ONBOARDING),
                "pendingApprovals", workflowSteps.countByStatus(ApprovalStatus.PENDING),
                "pendingLeaveRequests", leaveRequests.countByStatus(LeaveStatus.PENDING),
                "notificationSuccessRate", notifications.successRate()
        );
    }

    @GetMapping("/metrics/export.csv")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    ResponseEntity<String> exportCsv() {
        Map<String, Object> metrics = metrics();
        StringBuilder csv = new StringBuilder("metric,value\n");
        metrics.forEach((key, value) -> csv.append(key).append(',').append(value).append('\n'));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nexushr-dashboard.csv")
                .body(csv.toString());
    }
}
