package com.nexushr.payroll;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payslips")
public class Payslip extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID payrollRunId;
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSalary;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal taxDeduction;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal otherDeductions;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;

    public UUID getId() { return id; }
    public UUID getPayrollRunId() { return payrollRunId; }
    public void setPayrollRunId(UUID payrollRunId) { this.payrollRunId = payrollRunId; }
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }
    public BigDecimal getTaxDeduction() { return taxDeduction; }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction = taxDeduction; }
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
}
