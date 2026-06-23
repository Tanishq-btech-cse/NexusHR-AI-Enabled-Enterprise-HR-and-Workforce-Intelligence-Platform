package com.nexushr.employee;

import com.nexushr.security.AppRole;
import com.nexushr.security.AppUser;
import com.nexushr.security.AppUserRepository;
import com.nexushr.attendance.LeaveBalance;         // 🌟 Import your LeaveBalance entity
import com.nexushr.attendance.LeaveBalanceRepository; // 🌟 Import your LeaveBalance repository
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final EmployeeDocumentRepository documents;
    private final WorkflowStepRepository workflowSteps;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LeaveBalanceRepository leaveBalanceRepository; // 🌟 Injected repository

    public EmployeeService(EmployeeRepository employees,
                           EmployeeDocumentRepository documents,
                           WorkflowStepRepository workflowSteps,
                           AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           LeaveBalanceRepository leaveBalanceRepository) {
        this.employees = employees;
        this.documents = documents;
        this.workflowSteps = workflowSteps;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(EmployeeStatus.ONBOARDING);
        Employee saved = employees.save(employee);

        // 🌟 AUTOMATIC CREDENTIAL PROVISIONING
        if (!userRepository.existsByEmail(employee.getWorkEmail())) {
            AppUser securityUser = new AppUser();
            securityUser.setEmail(employee.getWorkEmail());
            securityUser.setPassword(passwordEncoder.encode("password123"));
            securityUser.setRoles(Set.of(AppRole.EMPLOYEE));
            userRepository.save(securityUser);
        }

        // 🌟 AUTOMATIC LEAVE BALANCE INITIALIZATION
        // Fixed: Initializing with clean BigDecimal allocations
        initDefaultLeaveBalance(saved.getId(), "ANNUAL", 21);
        initDefaultLeaveBalance(saved.getId(), "SICK", 12);
        initDefaultLeaveBalance(saved.getId(), "CASUAL", 7);

        // Onboarding lifecycle sequence
        createStep(saved.getId(), "ONBOARDING", "HR profile validation", 1);
        createStep(saved.getId(), "ONBOARDING", "Manager role assignment", 2);
        createStep(saved.getId(), "ONBOARDING", "IT access provisioning", 3);
        return saved;
    }

    // 🌟 FIXED HELPER METHOD: Converts int parameters to BigDecimal wrappers cleanly
    private void initDefaultLeaveBalance(UUID employeeId, String leaveType, int openingBalance) {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployeeId(employeeId);
        balance.setLeaveType(leaveType);

        // Converts the primitive int data streams into valid BigDecimal objects
        balance.setOpeningBalance(java.math.BigDecimal.valueOf(openingBalance));
        balance.setConsumed(java.math.BigDecimal.ZERO);

        leaveBalanceRepository.save(balance);
    }
    public List<Employee> list() {
        return employees.findAll();
    }

    // 🌟 ADD THIS METHOD to resolve self-profile context queries
    public Employee findByEmail(String email) {
        return employees.findAll().stream()
                .filter(e -> e.getWorkEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Employee profile matching email not found"));
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
    @Transactional
    public void delete(UUID id) {
        Employee employee = get(id);

        // 1. Purge matching login security user accounts by email context tracking
        userRepository.findByEmail(employee.getWorkEmail())
                .ifPresent(userRepository::delete);

        // 2. Purge bound workflow tracker artifacts
        List<WorkflowStep> steps = workflowSteps.findByEmployeeIdOrderByStepOrder(id);
        workflowSteps.deleteAll(steps);

        // 3. Purge accompanying profile track entries
        employees.delete(employee);
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