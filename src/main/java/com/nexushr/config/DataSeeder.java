package com.nexushr.config;

import com.nexushr.security.AppUser;
import com.nexushr.security.AppUserRepository;
import com.nexushr.security.AppRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Safe check: Only seed if the account doesn't exist yet
        if (userRepository.findByEmailIgnoreCase("employee@nexushr.local").isEmpty()) {
            AppUser employee = new AppUser();
            employee.setEmail("employee@nexushr.local");
            // Encrypts the password natively to pass Spring Security checks
            employee.setPasswordHash(passwordEncoder.encode("password123"));
            employee.setEnabled(true);

            // Assigns the standard staff authorization role
            employee.setRoles(Set.of(AppRole.EMPLOYEE));

            userRepository.save(employee);
            System.out.println("✅ Standard employee account seeded successfully: employee@nexushr.local");
        }
    }
}