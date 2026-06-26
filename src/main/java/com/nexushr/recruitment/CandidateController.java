package com.nexushr.recruitment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recruitment")
public class CandidateController {

    private final CandidateRepository repository;

    public CandidateController(CandidateRepository repository) {
        this.repository = repository;
    }

    // HR can view all candidates
    @GetMapping("/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public List<Candidate> getAllCandidates() {
        return repository.findAll();
    }

    // Public endpoint for applicants to submit their details (No Auth required)
    @PostMapping("/apply")
    public Candidate apply(@RequestBody Candidate candidate) {
        return repository.save(candidate);
    }

    // HR can update the status (e.g., to "INTERVIEWING")
    @PatchMapping("/candidates/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Candidate updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        Candidate candidate = repository.findById(id).orElseThrow();
        candidate.setStatus(payload.get("status"));
        return repository.save(candidate);
    }
}