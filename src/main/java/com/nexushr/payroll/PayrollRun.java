package com.nexushr.payroll;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs")
public class PayrollRun extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private int payrollYear;
    @Column(nullable = false)
    private int payrollMonth;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    public UUID getId() { return id; }
    public int getPayrollYear() { return payrollYear; }
    public void setPayrollYear(int payrollYear) { this.payrollYear = payrollYear; }
    public int getPayrollMonth() { return payrollMonth; }
    public void setPayrollMonth(int payrollMonth) { this.payrollMonth = payrollMonth; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public YearMonth period() { return YearMonth.of(payrollYear, payrollMonth); }
}
