package com.nexushr.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 👑 Restrict viewing the absolute list to core management layers
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    List<Employee> list() {
        return service.list();
    }

    // 👑 Employee provisioning is restricted to admin/HR
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
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

    // 🌟 UPDATED: Employee can view their own profile, managers/HR can view any profile
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#id)")
    Employee get(@PathVariable UUID id) {
        return service.get(id);
    }

    // 👑 Corporate assignment adjustments are restricted
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    Employee assignRole(@PathVariable UUID id, @Valid @RequestBody RoleAssignmentRequest request) {
        return service.assignRole(id, request.department(), request.designation(), request.managerId());
    }

    // 👑 Termination/offboarding process trigger restricted
    @PostMapping("/{id}/offboarding")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    Employee offboard(@PathVariable UUID id) {
        return service.offboard(id);
    }

    // 🌟 UPDATED: Employees can only upload checking files or credentials to their own record track
    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#id))")
    EmployeeDocument uploadDocument(@PathVariable UUID id, @Valid @RequestBody DocumentRequest request) {
        EmployeeDocument document = new EmployeeDocument();
        document.setDocumentType(request.documentType());
        document.setFileName(request.fileName());
        document.setStorageUrl(request.storageUrl());
        return service.addDocument(id, document);
    }

    // 🌟 UPDATED: Employees can view their own verified documents, managers/HR can view any profile document package
    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#id)")
    List<EmployeeDocument> documents(@PathVariable UUID id) {
        return service.documents(id);
    }

    // 👑 Lifecycle tracking updates remain visible only to internal staff structures
    @GetMapping("/{id}/workflow")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    List<WorkflowStep> workflow(@PathVariable UUID id) {
        return service.workflow(id);
    }

    // 👑 Workflow evaluations remain locked down completely to decision roles
    @PostMapping("/workflow/{stepId}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','PAYROLL')")
    WorkflowStep decide(@PathVariable UUID stepId, @Valid @RequestBody DecisionRequest request) {
        return service.decide(stepId, request.status(), request.approverId(), request.comment());
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