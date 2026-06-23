package com.nexushr.payroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {
    private final PayrollService service;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL')")
    PayrollRun calculate(@Valid @RequestBody PayrollRunRequest request) {
        return service.calculate(request.year(), request.month());
    }

    @PostMapping("/runs/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL')")
    PayrollRun approve(@PathVariable UUID id) {
        return service.approve(id);
    }

    @GetMapping("/runs/{id}/payslips")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL','HR')")
    List<Payslip> payslips(@PathVariable UUID id) {
        return service.payslips(id);
    }

    // 🌟 FIX: Employee can only access their personal payslips
    @GetMapping("/employees/{employeeId}/payslips")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL','HR') or @hrSecurity.isSelf(#employeeId)")
    List<Payslip> employeePayslips(@PathVariable UUID employeeId) {
        return service.employeePayslips(employeeId);
    }

    @GetMapping("/runs/{id}/export.csv")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL')")
    ResponseEntity<String> exportCsv(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslips-" + id + ".csv")
                .body(service.exportCsv(id));
    }

    public record PayrollRunRequest(@NotNull @Min(2024) Integer year, @NotNull @Min(1) @Max(12) Integer month) {}
}