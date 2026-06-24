package com.nexushr.attendance;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private LocalDate workDate;
    private Instant checkInAt;
    private Instant checkOutAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    private String biometricDeviceId;
    public Instant getCheckOutAt() { return checkOutAt; }
    public String getBiometricDeviceId() { return biometricDeviceId; }
}
