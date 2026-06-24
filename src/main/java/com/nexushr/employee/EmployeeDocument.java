package com.nexushr.employee;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "employee_documents")
public class EmployeeDocument extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private String documentType;
    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String storageUrl;
    private boolean verified;
    public UUID getId() {
        return id;
    }
    public UUID getEmployeeId() {
        return employeeId;
    }
    public String getDocumentType() {
        return documentType;
    }
    public String getFileName() {
        return fileName;
    }
    public String getStorageUrl() {
        return storageUrl;
    }
    public boolean isVerified() {
        return verified;
    }
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
