package com.nexushr.employee;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final EmployeeDocumentRepository documentRepository;

    private final String UPLOAD_DIR = System.getProperty("java.io.tmpdir")
            + (System.getProperty("java.io.tmpdir").endsWith("/") ? "" : "/") + "uploads/";

    public DocumentService(EmployeeDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;

        // Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // 1. Upload logic for Employee
    public EmployeeDocument uploadDocument(MultipartFile file, String documentType, UUID employeeId) throws IOException {
        // Create unique file name to prevent overwriting
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

        // Save file to disk
        Files.write(filePath, file.getBytes());

        // Create and save your entity
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployeeId(employeeId);
        doc.setDocumentType(documentType);
        doc.setFileName(file.getOriginalFilename());
        doc.setStorageUrl(filePath.toString()); // Saving the local path
        doc.setVerified(false); // Defaults to unverified (Pending Manager Approval)

        return documentRepository.save(doc);
    }

    // 2. Verification logic for Manager
    public EmployeeDocument verifyDocument(UUID documentId, boolean isVerified) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setVerified(isVerified);
        return documentRepository.save(doc);
    }

    // 3. Fetch pending for Manager Dashboard
    public List<EmployeeDocument> getPendingDocuments() {
        return documentRepository.findByVerifiedFalse();
    }

    // 4. Fetch all documents for a specific employee
    public List<EmployeeDocument> getMyDocuments(UUID employeeId) {
        return documentRepository.findByEmployeeId(employeeId);
    }

    // 5. Delete a document (with ownership security check)
    public void deleteDocument(UUID documentId, UUID employeeId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Security check: Prevent Employee A from deleting Employee B's files
        if (!doc.getEmployeeId().equals(employeeId)) {
            throw new SecurityException("You do not have permission to delete this document.");
        }

        // Delete the physical file from the server's hard drive
        try {
            Files.deleteIfExists(Paths.get(doc.getStorageUrl()));
        } catch (IOException e) {
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        // Delete the record from the database
        documentRepository.delete(doc);
    }
}