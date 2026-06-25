package com.nexushr.attendance;

import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {
    private final AttendanceRecordRepository attendance;
    private final LeaveRequestRepository leaves;
    private final LeaveBalanceRepository balances;
    private final EmployeeRepository employeeRepository; // 🌟 Added Employee Repository

    // 🌟 Updated constructor to include EmployeeRepository
    public AttendanceService(AttendanceRecordRepository attendance,
                             LeaveRequestRepository leaves,
                             LeaveBalanceRepository balances,
                             EmployeeRepository employeeRepository) {
        this.attendance = attendance;
        this.leaves = leaves;
        this.balances = balances;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public AttendanceRecord biometricPunch(UUID employeeId, String deviceId) {
        LocalDate today = LocalDate.now();

        // 1. Find existing record for today or create a new one
        AttendanceRecord record = attendance.findByEmployeeIdAndWorkDate(employeeId, today).orElseGet(() -> {
            AttendanceRecord created = new AttendanceRecord();
            created.setEmployeeId(employeeId);
            created.setWorkDate(today);
            return created;
        });

        // 2. Fetch the employee profile to check their Work Model (Remote vs Office)
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (record.getCheckInAt() == null) {
            // First punch of the day
            record.setCheckInAt(Instant.now());
            // Tentatively set their status based on their profile
            record.setStatus(employee.isRemote() ? AttendanceStatus.REMOTE : AttendanceStatus.PRESENT);
        } else {
            // Second punch of the day (Punch Out)
            record.setCheckOutAt(Instant.now());

            // 🌟 3. Strict Time Window Business Rules Engine
            LocalTime punchInStart = LocalTime.of(8, 0);   // 8:00 AM
            LocalTime punchInEnd = LocalTime.of(9, 0);     // 9:00 AM
            LocalTime punchOutStart = LocalTime.of(17, 0); // 5:00 PM
            LocalTime punchOutEnd = LocalTime.of(18, 0);   // 6:00 PM

            // Convert server Instant time to Local Time for comparison
            ZoneId zone = ZoneId.systemDefault();
            LocalTime actualPunchIn = record.getCheckInAt().atZone(zone).toLocalTime();
            LocalTime actualPunchOut = record.getCheckOutAt().atZone(zone).toLocalTime();

            // Evaluate if punches fell exactly inside the allowed windows
            boolean isValidPunchIn = !actualPunchIn.isBefore(punchInStart) && !actualPunchIn.isAfter(punchInEnd);
            boolean isValidPunchOut = !actualPunchOut.isBefore(punchOutStart) && !actualPunchOut.isAfter(punchOutEnd);

            // 4. Assign Final Status
            if (isValidPunchIn && isValidPunchOut) {
                // They followed the rules, set status based on their assigned work model
                record.setStatus(employee.isRemote() ? AttendanceStatus.REMOTE : AttendanceStatus.PRESENT);
            } else {
                // They missed the compliance windows, automatically flag as absent
                record.setStatus(AttendanceStatus.ABSENT);
            }
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
        List<LeaveRequest> pendingRequestsList = leaves.findByStatus(LeaveStatus.PENDING);

        return Map.of(
                "date", date,
                "present", attendance.countByWorkDateAndStatus(date, AttendanceStatus.PRESENT),
                "remote", attendance.countByWorkDateAndStatus(date, AttendanceStatus.REMOTE),
                "absent", attendance.countByWorkDateAndStatus(date, AttendanceStatus.ABSENT),
                "pendingLeaves", leaves.countByStatus(LeaveStatus.PENDING),
                "pendingRequests", pendingRequestsList
        );
    }
}