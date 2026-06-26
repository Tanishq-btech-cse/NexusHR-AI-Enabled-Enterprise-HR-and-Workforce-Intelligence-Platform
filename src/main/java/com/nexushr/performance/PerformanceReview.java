package com.nexushr.performance;

import com.nexushr.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview extends AuditableEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private String cycle;
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal managerRating;
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal peerRating;
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal selfRating;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.DRAFT;
    private String feedback;
    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
    public BigDecimal getManagerRating() { return managerRating; }
    public void setManagerRating(BigDecimal managerRating) { this.managerRating = managerRating; }
    public BigDecimal getPeerRating() { return peerRating; }
    public void setPeerRating(BigDecimal peerRating) { this.peerRating = peerRating; }
    public BigDecimal getSelfRating() { return selfRating; }
    public void setSelfRating(BigDecimal selfRating) { this.selfRating = selfRating; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
