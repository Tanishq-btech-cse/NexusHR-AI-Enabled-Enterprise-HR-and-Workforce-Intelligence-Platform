package com.nexushr.performance;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class PerformanceService {
    private final GoalRepository goals;
    private final PerformanceReviewRepository reviews;

    public PerformanceService(GoalRepository goals, PerformanceReviewRepository reviews) {
        this.goals = goals;
        this.reviews = reviews;
    }

    public Goal createGoal(Goal goal) {
        return goals.save(goal);
    }

    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        BigDecimal score = review.getManagerRating().multiply(BigDecimal.valueOf(0.5))
                .add(review.getPeerRating().multiply(BigDecimal.valueOf(0.3)))
                .add(review.getSelfRating().multiply(BigDecimal.valueOf(0.2)))
                .multiply(BigDecimal.valueOf(20))
                .setScale(2, RoundingMode.HALF_UP);
        review.setScore(score);
        review.setStatus(ReviewStatus.SUBMITTED);
        return reviews.save(review);
    }

    public List<Goal> goals(UUID employeeId) {
        return goals.findByEmployeeId(employeeId);
    }

    public List<PerformanceReview> scorecard(UUID employeeId) {
        return reviews.findByEmployeeIdOrderByCycle(employeeId);
    }
}
