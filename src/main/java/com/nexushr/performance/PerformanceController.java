package com.nexushr.performance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {
    private final PerformanceService service;

    public PerformanceController(PerformanceService service) {
        this.service = service;
    }

    @PostMapping("/goals")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or (hasRole('EMPLOYEE') and @hrSecurity.isSelf(#request.employeeId()))")
    Goal createGoal(@Valid @RequestBody GoalRequest request) {
        Goal goal = new Goal();
        goal.setEmployeeId(request.employeeId());
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setDueDate(request.dueDate());
        return service.createGoal(goal);
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    PerformanceReview createReview(@Valid @RequestBody ReviewRequest request) {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(request.employeeId());
        review.setCycle(request.cycle());
        review.setManagerRating(request.managerRating());
        review.setPeerRating(request.peerRating());
        review.setSelfRating(request.selfRating());
        review.setFeedback(request.feedback());
        return service.createReview(review);
    }

    @GetMapping("/employees/{employeeId}/goals")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#employeeId)")
    List<Goal> goals(@PathVariable UUID employeeId) {
        return service.goals(employeeId);
    }

    @GetMapping("/employees/{employeeId}/scorecard")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER') or @hrSecurity.isSelf(#employeeId)")
    List<PerformanceReview> scorecard(@PathVariable UUID employeeId) {
        return service.scorecard(employeeId);
    }

    public record GoalRequest(@NotNull UUID employeeId, @NotBlank String title, String description, LocalDate dueDate) {}
    public record ReviewRequest(@NotNull UUID employeeId, @NotBlank String cycle, @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal managerRating, @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal peerRating, @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal selfRating, String feedback) {}
}