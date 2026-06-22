package com.nexushr.insights;

import com.nexushr.attendance.LeaveRequestRepository;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.performance.PerformanceReview;
import com.nexushr.performance.PerformanceReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkforceInsightsService {
    private final EmployeeRepository employees;
    private final PerformanceReviewRepository reviews;
    private final LeaveRequestRepository leaveRequests;

    public WorkforceInsightsService(EmployeeRepository employees, PerformanceReviewRepository reviews,
                                    LeaveRequestRepository leaveRequests) {
        this.employees = employees;
        this.reviews = reviews;
        this.leaveRequests = leaveRequests;
    }

    public WorkforceInsight employeeInsight(UUID employeeId) {
        Employee employee = employees.findById(employeeId).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        List<PerformanceReview> history = reviews.findByEmployeeIdOrderByCycle(employeeId);
        BigDecimal latestScore = history.isEmpty() ? BigDecimal.valueOf(75) : history.getLast().getScore();
        BigDecimal engagement = latestScore.multiply(BigDecimal.valueOf(0.75))
                .add(BigDecimal.valueOf(leaveRequests.findByEmployeeId(employeeId).size()).min(BigDecimal.TEN).multiply(BigDecimal.valueOf(2.5)))
                .min(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal attritionRisk = BigDecimal.valueOf(100).subtract(engagement).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        List<String> skillGaps = skillGaps(employee.getProfile());
        return new WorkforceInsight(employeeId, attritionRisk, engagement, skillGaps, recommendations(attritionRisk, skillGaps));
    }

    public List<WorkforceInsight> organizationInsights() {
        return employees.findAll().stream().map(employee -> employeeInsight(employee.getId())).toList();
    }

    private List<String> skillGaps(Map<String, Object> profile) {
        Object skills = profile.get("skills");
        Object required = profile.get("requiredSkills");
        if (!(skills instanceof List<?> current) || !(required instanceof List<?> expected)) {
            return List.of("role-specific skill matrix missing");
        }
        return expected.stream()
                .map(Object::toString)
                .filter(skill -> current.stream().map(Object::toString).noneMatch(skill::equalsIgnoreCase))
                .toList();
    }

    private List<String> recommendations(BigDecimal attritionRisk, List<String> skillGaps) {
        List<String> recommendations = new ArrayList<>();
        if (attritionRisk.compareTo(BigDecimal.valueOf(40)) > 0) {
            recommendations.add("Schedule manager check-in and compensation review");
        }
        if (!skillGaps.isEmpty()) {
            recommendations.add("Assign focused learning plan for " + String.join(", ", skillGaps));
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Continue current engagement and growth plan");
        }
        return recommendations;
    }
}
