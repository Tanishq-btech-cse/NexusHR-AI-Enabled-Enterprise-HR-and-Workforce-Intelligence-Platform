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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.MalformedURLException;

@Service
public class DocumentService {

    private final EmployeeDocumentRepository documentRepository;

    private final String UPLOAD_DIR = System.getProperty("java.io.tmpdir")
            + (System.getProperty("java.io.tmpdir").endsWith("/") ? "" : "/") + "uploads/";
    public DocumentService(EmployeeDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public EmployeeDocument uploadDocument(MultipartFile file, String documentType, UUID employeeId) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
        Files.write(filePath, file.getBytes());
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployeeId(employeeId);
        doc.setDocumentType(documentType);
        doc.setFileName(file.getOriginalFilename());
        doc.setStorageUrl(filePath.toString());
        doc.setVerified(false);

        return documentRepository.save(doc);
    }

    public EmployeeDocument verifyDocument(UUID documentId, boolean isVerified) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setVerified(isVerified);
        return documentRepository.save(doc);
    }

    public List<EmployeeDocument> getPendingDocuments() {
        return documentRepository.findByVerifiedFalse();
    }

    public List<EmployeeDocument> getMyDocuments(UUID employeeId) {
        return documentRepository.findByEmployeeId(employeeId);
    }

    public void deleteDocument(UUID documentId, UUID employeeId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getEmployeeId().equals(employeeId)) {
            throw new SecurityException("You do not have permission to delete this document.");
        }
        try {
            Files.deleteIfExists(Paths.get(doc.getStorageUrl()));
        } catch (IOException e) {
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }
        documentRepository.delete(doc);
    }
    public EmployeeDocument getDocumentById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public Resource loadDocumentAsResource(UUID documentId) {
        EmployeeDocument doc = getDocumentById(documentId);
        try {
            Path filePath = Paths.get(doc.getStorageUrl());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found on server.");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path invalid.", ex);
        }
    }
}