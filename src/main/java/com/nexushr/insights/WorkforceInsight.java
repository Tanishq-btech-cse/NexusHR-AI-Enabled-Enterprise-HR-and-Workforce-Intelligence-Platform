package com.nexushr.insights;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WorkforceInsight(UUID employeeId, BigDecimal attritionRisk, BigDecimal engagementScore,
                               List<String> skillGaps, List<String> recommendations) {
}
