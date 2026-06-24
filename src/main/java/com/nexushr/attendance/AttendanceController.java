package com.nexushr.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping("/biometric-punch")
    @PreAuthorize("hasAnyRole('ADMIN','HR') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#request.employeeId()))")
    AttendanceRecord punch(@Valid @RequestBody PunchRequest request) {
        return service.biometricPunch(request.employeeId(), request.deviceId());
    }

    @PostMapping("/leave-requests")
    @PreAuthorize("hasAnyRole('ADMIN','HR') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#request.employeeId()))")
    LeaveRequest requestLeave(@Valid @RequestBody LeaveRequestDto request) {
        return service.requestLeave(request.employeeId(), request.leaveType(), request.startDate(), request.endDate(), request.reason());
    }

    @PostMapping("/leave-requests/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    LeaveRequest decideLeave(@PathVariable UUID id, @Valid @RequestBody LeaveDecisionRequest request) {
        return service.decideLeave(id, request.status(), request.approverId());
    }

    @GetMapping("/employees/{employeeId}/leave-balances")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#employeeId)")
    List<LeaveBalance> balances(@PathVariable UUID employeeId) {
        return service.balances(employeeId);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    Map<String, Object> dashboard(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.dashboard(date);
    }

    public record PunchRequest(@NotNull UUID employeeId, @NotBlank String deviceId) {}
    public record LeaveRequestDto(@NotNull UUID employeeId, @NotBlank String leaveType, @NotNull LocalDate startDate, @NotNull LocalDate endDate, String reason) {}
    public record LeaveDecisionRequest(@NotNull LeaveStatus status, UUID approverId) {}
}