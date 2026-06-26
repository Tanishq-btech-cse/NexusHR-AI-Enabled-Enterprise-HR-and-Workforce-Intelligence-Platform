package com.nexushr.recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
}