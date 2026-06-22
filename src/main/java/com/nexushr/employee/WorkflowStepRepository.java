package com.nexushr.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    List<WorkflowStep> findByEmployeeIdOrderByStepOrder(UUID employeeId);
    long countByStatus(ApprovalStatus status);
}
