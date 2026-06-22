package com.nexushr.payroll;

import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.employee.EmployeeStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class PayrollService {
    private static final BigDecimal DEFAULT_MONTHLY_SALARY = BigDecimal.valueOf(75000);
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);

    private final PayrollRunRepository runs;
    private final PayslipRepository payslips;
    private final EmployeeRepository employees;

    public PayrollService(PayrollRunRepository runs, PayslipRepository payslips, EmployeeRepository employees) {
        this.runs = runs;
        this.payslips = payslips;
        this.employees = employees;
    }

    @Transactional
    public PayrollRun calculate(int year, int month) {
        PayrollRun run = new PayrollRun();
        run.setPayrollYear(year);
        run.setPayrollMonth(month);
        run.setStatus(PayrollStatus.CALCULATED);
        PayrollRun savedRun = runs.save(run);
        employees.findAll().stream()
                .filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE || employee.getStatus() == EmployeeStatus.ONBOARDING)
                .map(employee -> payslip(savedRun.getId(), employee))
                .forEach(payslips::save);
        return savedRun;
    }

    @Transactional
    public PayrollRun approve(UUID runId) {
        PayrollRun run = runs.findById(runId).orElseThrow(() -> new EntityNotFoundException("Payroll run not found"));
        run.setStatus(PayrollStatus.APPROVED);
        return run;
    }

    public List<Payslip> payslips(UUID runId) {
        return payslips.findByPayrollRunId(runId);
    }

    public List<Payslip> employeePayslips(UUID employeeId) {
        return payslips.findByEmployeeId(employeeId);
    }

    public String exportCsv(UUID runId) {
        StringBuilder csv = new StringBuilder("employeeId,grossSalary,taxDeduction,otherDeductions,netSalary\n");
        payslips(runId).forEach(p -> csv.append(p.getEmployeeId()).append(',')
                .append(p.getGrossSalary()).append(',')
                .append(p.getTaxDeduction()).append(',')
                .append(p.getOtherDeductions()).append(',')
                .append(p.getNetSalary()).append('\n'));
        return csv.toString();
    }

    private Payslip payslip(UUID runId, Employee employee) {
        BigDecimal gross = monthlySalary(employee);
        BigDecimal tax = gross.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal other = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Payslip payslip = new Payslip();
        payslip.setPayrollRunId(runId);
        payslip.setEmployeeId(employee.getId());
        payslip.setGrossSalary(gross);
        payslip.setTaxDeduction(tax);
        payslip.setOtherDeductions(other);
        payslip.setNetSalary(gross.subtract(tax).subtract(other));
        return payslip;
    }

    private BigDecimal monthlySalary(Employee employee) {
        Object salary = employee.getProfile().get("monthlySalary");
        if (salary instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return DEFAULT_MONTHLY_SALARY.setScale(2, RoundingMode.HALF_UP);
    }
}
