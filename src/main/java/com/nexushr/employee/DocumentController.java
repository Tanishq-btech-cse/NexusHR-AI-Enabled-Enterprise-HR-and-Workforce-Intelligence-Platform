package com.nexushr.employee;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ---------------------------------------------------------
    // EMPLOYEE ENDPOINTS
    // ---------------------------------------------------------

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<EmployeeDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("employeeId") UUID employeeId) {

        try {
            EmployeeDocument savedDoc = documentService.uploadDocument(file, documentType, employeeId);
            return ResponseEntity.ok(savedDoc);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ---------------------------------------------------------
    // MANAGER / ADMIN ENDPOINTS
    // ---------------------------------------------------------

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeDocument>> getPendingDocuments() {
        return ResponseEntity.ok(documentService.getPendingDocuments());
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeDocument> verifyDocument(
            @PathVariable UUID id,
            @RequestParam boolean verified) {

        EmployeeDocument updatedDoc = documentService.verifyDocument(id, verified);
        return ResponseEntity.ok(updatedDoc);
    }

    @GetMapping("/me")
    public ResponseEntity<List<EmployeeDocument>> getMyDocuments(@RequestParam UUID employeeId) {
        return ResponseEntity.ok(documentService.getMyDocuments(employeeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable UUID id,
            @RequestParam UUID employeeId) {

        try {
            documentService.deleteDocument(id, employeeId);
            return ResponseEntity.ok().body("{\"message\": \"Document deleted successfully\"}");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) {
        Resource resource = documentService.loadDocumentAsResource(id);
        EmployeeDocument doc = documentService.getDocumentById(id);

        return ResponseEntity.ok()
                // "inline" tells the browser to try and open it (like a PDF or Image) instead of forcing a download
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}