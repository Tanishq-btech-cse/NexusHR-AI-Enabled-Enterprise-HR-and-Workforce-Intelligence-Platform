package com.nexushr.employee;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private String employeeCode;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String workEmail;
    private String department;
    private String designation;
    private UUID managerId;
    private LocalDate joiningDate;
    private LocalDate exitDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.DRAFT;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> profile = new HashMap<>();
    public UUID getId() {
        return id;
    }
    public String getEmployeeCode() {
        return employeeCode;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getWorkEmail() {
        return workEmail;
    }
    public String getDepartment() {
        return department;
    }
    public String getDesignation() {
        return designation;
    }
    public UUID getManagerId() {
        return managerId;
    }
    public LocalDate getJoiningDate() {
        return joiningDate;
    }
    public LocalDate getExitDate() {
        return exitDate;
    }
    public EmployeeStatus getStatus() {
        return status;
    }
    public Map<String, Object> getProfile() {
        return profile;
    }
}
