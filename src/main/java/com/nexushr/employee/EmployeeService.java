package com.nexushr.employee;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final EmployeeDocumentRepository documents;
    private final WorkflowStepRepository workflowSteps;

    public EmployeeService(EmployeeRepository employees, EmployeeDocumentRepository documents, WorkflowStepRepository workflowSteps) {
        this.employees = employees;
        this.documents = documents;
        this.workflowSteps = workflowSteps;
    }

    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(EmployeeStatus.ONBOARDING);
        Employee saved = employees.save(employee);
        createStep(saved.getId(), "ONBOARDING", "HR profile validation", 1);
        createStep(saved.getId(), "ONBOARDING", "Manager role assignment", 2);
        createStep(saved.getId(), "ONBOARDING", "IT access provisioning", 3);
        return saved;
    }

    public List<Employee> list() {
        return employees.findAll();
    }

    public Employee get(UUID id) {
        return employees.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    @Transactional
    public Employee assignRole(UUID id, String department, String designation, UUID managerId) {
        Employee employee = get(id);
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setManagerId(managerId);
        return employee;
    }

    @Transactional
    public Employee offboard(UUID id) {
        Employee employee = get(id);
        employee.setStatus(EmployeeStatus.OFFBOARDING);
        createStep(employee.getId(), "OFFBOARDING", "Manager clearance", 1);
        createStep(employee.getId(), "OFFBOARDING", "Payroll settlement", 2);
        createStep(employee.getId(), "OFFBOARDING", "IT access revocation", 3);
        return employee;
    }

    @Transactional
    public EmployeeDocument addDocument(UUID employeeId, EmployeeDocument document) {
        get(employeeId);
        document.setEmployeeId(employeeId);
        return documents.save(document);
    }

    public List<EmployeeDocument> documents(UUID employeeId) {
        get(employeeId);
        return documents.findByEmployeeId(employeeId);
    }

    @Transactional
    public WorkflowStep decide(UUID stepId, ApprovalStatus status, UUID approverId, String comment) {
        WorkflowStep step = workflowSteps.findById(stepId).orElseThrow(() -> new EntityNotFoundException("Workflow step not found"));
        step.setStatus(status);
        step.setApproverId(approverId);
        step.setComment(comment);
        step.setDecidedAt(Instant.now());
        return step;
    }

    public List<WorkflowStep> workflow(UUID employeeId) {
        get(employeeId);
        return workflowSteps.findByEmployeeIdOrderByStepOrder(employeeId);
    }

    private void createStep(UUID employeeId, String type, String name, int order) {
        WorkflowStep step = new WorkflowStep();
        step.setEmployeeId(employeeId);
        step.setWorkflowType(type);
        step.setStepName(name);
        step.setStepOrder(order);
        workflowSteps.save(step);
    }
}
