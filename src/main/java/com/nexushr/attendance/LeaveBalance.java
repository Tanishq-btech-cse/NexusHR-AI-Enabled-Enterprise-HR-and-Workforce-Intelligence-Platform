package com.nexushr.attendance;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "leave_balances")
public class LeaveBalance extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private String leaveType;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal accrued = BigDecimal.ZERO;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal consumed = BigDecimal.ZERO;

    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public String getLeaveType() { return leaveType; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getAccrued() { return accrued; }
    public void setAccrued(BigDecimal accrued) { this.accrued = accrued; }
    public BigDecimal getConsumed() { return consumed; }
    public BigDecimal available() { return openingBalance.add(accrued).subtract(consumed); }
}
