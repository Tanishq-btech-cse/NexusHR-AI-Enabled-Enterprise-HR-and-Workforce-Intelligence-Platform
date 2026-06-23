package com.nexushr.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // 👑 Restrict viewing to management
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','ROLE_ADMIN','ROLE_HR')")
    List<Employee> list() {
        return service.list();
    }

    // 👑 Provisioning restricted to Admin/HR
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','ROLE_ADMIN','ROLE_HR')")
    Employee create(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = new Employee();
        employee.setEmployeeCode(request.employeeCode());
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setWorkEmail(request.workEmail());
        employee.setJoiningDate(request.joiningDate());
        employee.setProfile(request.profile() == null ? Map.of() : request.profile());
        return service.create(employee);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','ROLE_ADMIN') or @hrSecurity.isSelf(#id)")
    Employee get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','HR','ROLE_ADMIN')")
    Employee assignRole(@PathVariable UUID id, @Valid @RequestBody RoleAssignmentRequest request) {
        return service.assignRole(id, request.department(), request.designation(), request.managerId());
    }

    @GetMapping("/me")
    public Employee getMe(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        return service.findByEmail(email);
    }

    @PostMapping("/{id}/offboarding")
    @PreAuthorize("hasAnyRole('ADMIN','HR','ROLE_ADMIN')")
    Employee offboard(@PathVariable UUID id) {
        return service.offboard(id);
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','ROLE_ADMIN') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#id))")
    EmployeeDocument uploadDocument(@PathVariable UUID id, @Valid @RequestBody DocumentRequest request) {
        EmployeeDocument document = new EmployeeDocument();
        document.setDocumentType(request.documentType());
        document.setFileName(request.fileName());
        document.setStorageUrl(request.storageUrl());
        return service.addDocument(id, document);
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','ROLE_ADMIN') or @hrSecurity.isSelf(#id)")
    List<EmployeeDocument> documents(@PathVariable UUID id) {
        return service.documents(id);
    }

    // 🌟 ADMIN ROLE MANAGEMENT ENDPOINTS
    @PutMapping("/{id}/security-role")
    @PreAuthorize("hasAnyRole('ADMIN','ROLE_ADMIN')")
    public void updateSecurityRole(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        service.updateSecurityRole(id, request.get("role"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROLE_ADMIN')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
    public record EmployeeCreateRequest(@NotBlank String employeeCode, @NotBlank String firstName,
                                        @NotBlank String lastName, @Email String workEmail,
                                        @NotNull LocalDate joiningDate, Map<String, Object> profile) {
    }

    public record RoleAssignmentRequest(@NotBlank String department, @NotBlank String designation, UUID managerId) {
    }

    public record DocumentRequest(@NotBlank String documentType, @NotBlank String fileName, @NotBlank String storageUrl) {
    }

    public record DecisionRequest(@NotNull ApprovalStatus status, UUID approverId, String comment) {
    }
}