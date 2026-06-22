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
        // 💼 1. Seed Employee Account if missing
        if (userRepository.findByEmailIgnoreCase("employee@nexushr.local").isEmpty()) {
            AppUser employee = new AppUser();
            employee.setEmail("employee@nexushr.local");
            employee.setPasswordHash(passwordEncoder.encode("password123"));
            employee.setEnabled(true);
            employee.setRoles(Set.of(AppRole.EMPLOYEE));

            userRepository.save(employee);
            System.out.println("✅ Standard employee account seeded successfully: employee@nexushr.local");
        }

        // 👑 2. Seed Admin Account if missing
        if (userRepository.findByEmailIgnoreCase("admin@nexushr.local").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setEmail("admin@nexushr.local");
            admin.setPasswordHash(passwordEncoder.encode("admin123")); // Or your preferred admin pass
            admin.setEnabled(true);
            admin.setRoles(Set.of(AppRole.ADMIN)); // Ensure your AppRole enum has ADMIN

            userRepository.save(admin);
            System.out.println("👑 Administrative account seeded successfully: admin@nexushr.local");
        }
    }
}