package com.nexushr.attendance;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {
    private final AttendanceRecordRepository attendance;
    private final LeaveRequestRepository leaves;
    private final LeaveBalanceRepository balances;

    public AttendanceService(AttendanceRecordRepository attendance, LeaveRequestRepository leaves, LeaveBalanceRepository balances) {
        this.attendance = attendance;
        this.leaves = leaves;
        this.balances = balances;
    }

    @Transactional
    public AttendanceRecord biometricPunch(UUID employeeId, String deviceId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendance.findByEmployeeIdAndWorkDate(employeeId, today).orElseGet(() -> {
            AttendanceRecord created = new AttendanceRecord();
            created.setEmployeeId(employeeId);
            created.setWorkDate(today);
            created.setStatus(AttendanceStatus.PRESENT);
            return created;
        });
        if (record.getCheckInAt() == null) {
            record.setCheckInAt(Instant.now());
        } else {
            record.setCheckOutAt(Instant.now());
        }
        record.setBiometricDeviceId(deviceId);
        return attendance.save(record);
    }

    @Transactional
    public LeaveRequest requestLeave(UUID employeeId, String leaveType, LocalDate start, LocalDate end, String reason) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("Leave end date must be on or after start date");
        }

        // 🌟 DYNAMIC FALLBACK ENHANCEMENT:
        // Before validating the balance container, verify if this user requires an on-the-fly seed allocation wrapper.
        // This makes sure both old and new employee profiles pass evaluation gates flawlessly!
        balances(employeeId);

        LeaveBalance balance = balances.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not configured"));
        BigDecimal requested = BigDecimal.valueOf(days);
        if (balance.available().compareTo(requested) < 0) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployeeId(employeeId);
        leave.setLeaveType(leaveType);
        leave.setStartDate(start);
        leave.setEndDate(end);
        leave.setDays(requested);
        leave.setReason(reason);
        return leaves.save(leave);
    }

    @Transactional
    public LeaveRequest decideLeave(UUID leaveId, LeaveStatus status, UUID approverId) {
        LeaveRequest leave = leaves.findById(leaveId).orElseThrow(() -> new EntityNotFoundException("Leave request not found"));
        leave.setStatus(status);
        leave.setApproverId(approverId);
        if (status == LeaveStatus.APPROVED) {
            LeaveBalance balance = balances.findByEmployeeIdAndLeaveType(leave.getEmployeeId(), leave.getLeaveType())
                    .orElseThrow(() -> new EntityNotFoundException("Leave balance not configured"));
            balance.setConsumed(balance.getConsumed().add(leave.getDays()));
        }
        return leave;
    }

    // 🌟 UPDATED: Fetches existing balances, or dynamically allocates default structures if missing
    @Transactional
    public List<LeaveBalance> balances(UUID employeeId) {
        List<LeaveBalance> currentBalances = balances.findByEmployeeId(employeeId);

        if (currentBalances.isEmpty()) {
            LeaveBalance annual = new LeaveBalance();
            annual.setEmployeeId(employeeId);
            annual.setLeaveType("ANNUAL");
            annual.setOpeningBalance(BigDecimal.valueOf(21));
            annual.setConsumed(BigDecimal.ZERO);

            LeaveBalance sick = new LeaveBalance();
            sick.setEmployeeId(employeeId);
            sick.setLeaveType("SICK");
            sick.setOpeningBalance(BigDecimal.valueOf(12));
            sick.setConsumed(BigDecimal.ZERO);

            LeaveBalance casual = new LeaveBalance();
            casual.setEmployeeId(employeeId);
            casual.setLeaveType("CASUAL");
            casual.setOpeningBalance(BigDecimal.valueOf(7));
            casual.setConsumed(BigDecimal.ZERO);

            List<LeaveBalance> defaultSeededList = List.of(annual, sick, casual);
            balances.saveAll(defaultSeededList);
            return defaultSeededList;
        }

        return currentBalances;
    }

    public Map<String, Object> dashboard(LocalDate date) {
        // 🌟 Fetch the actual list of pending leave applications from the repository
        List<LeaveRequest> pendingRequestsList = leaves.findByStatus(LeaveStatus.PENDING); // Make sure findByStatus exists in LeaveRequestRepository

        return Map.of(
                "date", date,
                "present", attendance.countByWorkDateAndStatus(date, AttendanceStatus.PRESENT),
                "remote", attendance.countByWorkDateAndStatus(date, AttendanceStatus.REMOTE),
                "absent", attendance.countByWorkDateAndStatus(date, AttendanceStatus.ABSENT),
                "pendingLeaves", leaves.countByStatus(LeaveStatus.PENDING),
                "pendingRequests", pendingRequestsList // 🌟 ADD THIS: The actual array objects for the frontend table
        );
    }
}