package com.nexushr.insights;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insights")
public class InsightsController {
    private final WorkforceInsightsService service;

    public InsightsController(WorkforceInsightsService service) {
        this.service = service;
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    WorkforceInsight employeeInsight(@PathVariable UUID employeeId) {
        return service.employeeInsight(employeeId);
    }

    @GetMapping("/organization")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    List<WorkforceInsight> organizationInsights() {
        return service.organizationInsights();
    }
}
