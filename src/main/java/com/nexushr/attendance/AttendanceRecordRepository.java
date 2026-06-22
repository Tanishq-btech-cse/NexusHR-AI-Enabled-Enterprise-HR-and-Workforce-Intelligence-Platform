package com.nexushr.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate workDate);
    List<AttendanceRecord> findByWorkDate(LocalDate workDate);
    long countByWorkDateAndStatus(LocalDate workDate, AttendanceStatus status);
}
