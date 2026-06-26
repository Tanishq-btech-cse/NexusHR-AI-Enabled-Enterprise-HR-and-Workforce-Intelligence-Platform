package com.nexushr.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    List<Employee> list() {
        return service.list();
    }

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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#id)")
    Employee get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    Employee assignRole(@PathVariable UUID id, @Valid @RequestBody RoleAssignmentRequest request) {
        return service.assignRole(id, request.department(), request.designation(), request.managerId());
    }

    @GetMapping("/me")
    public Employee getMe(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        return service.findByEmail(email);
    }

    @PostMapping("/{id}/offboarding")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    Employee offboard(@PathVariable UUID id) {
        return service.offboard(id);
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#id))")
    EmployeeDocument uploadDocument(@PathVariable UUID id, @Valid @RequestBody DocumentRequest request) {
        EmployeeDocument document = new EmployeeDocument();
        document.setDocumentType(request.documentType());
        document.setFileName(request.fileName());
        document.setStorageUrl(request.storageUrl());
        return service.addDocument(id, document);
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#id)")
    List<EmployeeDocument> documents(@PathVariable UUID id) {
        return service.documents(id);
    }

    @GetMapping("/{id}/workflow")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    List<WorkflowStep> workflow(@PathVariable UUID id) {
        return service.workflow(id);
    }

    @PostMapping("/workflow/{stepId}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','PAYROLL')")
    WorkflowStep decide(@PathVariable UUID stepId, @Valid @RequestBody DecisionRequest request) {
        return service.decide(stepId, request.status(), request.approverId(), request.comment());
    }

    @PutMapping("/{id}/security-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void updateSecurityRole(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        String targetRole = request.getOrDefault("role", request.get("security-role"));
        if (targetRole == null || targetRole.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid role must be provided in the JSON payload.");
        }

        service.updateSecurityRole(id, targetRole);
    }

    @PatchMapping("/{id}/remote")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<?> toggleRemote(@PathVariable UUID id, @RequestParam boolean isRemote) {
        service.toggleRemoteStatus(id, isRemote);
        return ResponseEntity.ok().body("{\"message\": \"Work model updated successfully.\"}");
    }

    @GetMapping("/me/profile")
    public Employee getMyProfile(Principal principal) {
        return service.findByEmail(principal.getName());
    }

    @PostMapping("/me/profile")
    public Employee updateMyProfile(Principal principal, @RequestBody Employee payload) {
        Employee employee = service.findByEmail(principal.getName());

        employee.setFatherName(payload.getFatherName());
        employee.setMotherName(payload.getMotherName());
        employee.setAddress(payload.getAddress());
        employee.setAadhaarNumber(payload.getAadhaarNumber());
        employee.setPanNumber(payload.getPanNumber());

        employee.setProfileCompleted(true);
        employee.setEditRequestStatus("NONE");

        return service.updateEmployee(employee);
    }

    @PostMapping("/me/profile/request-edit")
    public Employee requestProfileEdit(Principal principal) {
        Employee employee = service.findByEmail(principal.getName());
        employee.setEditRequestStatus("PENDING");
        return service.updateEmployee(employee);
    }

    @GetMapping("/profile-edit-requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public List<Employee> getProfileEditRequests() {
        return service.getPendingProfileEditRequests();
    }

    @PostMapping("/{id}/profile/approve-edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Employee approveProfileEdit(@PathVariable UUID id) {
        Employee employee = service.get(id);
        employee.setEditRequestStatus("APPROVED");
        return service.updateEmployee(employee);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    public record EmployeeCreateRequest(@NotBlank String employeeCode, @NotBlank String firstName,
                                        @NotBlank String lastName, @Email String workEmail,
                                        @NotNull LocalDate joiningDate, Map<String, Object> profile) {
    }
    public record RoleAssignmentRequest(@NotBlank String department, @NotBlank String designation, UUID managerId) {
    }
    public record DocumentRequest(@NotBlank String documentType, @NotBlank String fileName,
                                  @NotBlank String storageUrl) {
    }
    public record DecisionRequest(@NotNull ApprovalStatus status, UUID approverId, String comment) {
    }
}