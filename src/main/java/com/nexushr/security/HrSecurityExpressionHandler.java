package com.nexushr.security;

import com.nexushr.employee.EmployeeRepository;
import com.nexushr.employee.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("hrSecurity")
public class HrSecurityExpressionHandler {

    private final EmployeeRepository employeeRepository;

    public HrSecurityExpressionHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public boolean isSelf(UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return false;
        }
        String loggedInEmail = userDetails.getUsername();
        return employeeRepository.findAll().stream()
                .filter(e -> e.getWorkEmail().equalsIgnoreCase(loggedInEmail))
                .map(Employee::getId)
                .findFirst()
                .map(id -> id.equals(employeeId))
                .orElse(false);
    }
}