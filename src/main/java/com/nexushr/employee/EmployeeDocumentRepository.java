package com.nexushr.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {

    // For Employees: Find their own documents
    List<EmployeeDocument> findByEmployeeId(UUID employeeId);

    // NEW: For Managers: Find all pending/unverified documents
    List<EmployeeDocument> findByVerifiedFalse();
}