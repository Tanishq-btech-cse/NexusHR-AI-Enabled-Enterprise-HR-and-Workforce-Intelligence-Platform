package com.nexushr.payroll;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
    List<Payslip> findByPayrollRunId(UUID payrollRunId);
    List<Payslip> findByEmployeeId(UUID employeeId);
}
