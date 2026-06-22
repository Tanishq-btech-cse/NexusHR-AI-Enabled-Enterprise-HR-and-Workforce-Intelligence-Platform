package com.nexushr.config;

import com.nexushr.security.AppRole;
import com.nexushr.security.AppUser;
import com.nexushr.security.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class BootstrapData {
    @Bean
    CommandLineRunner seedAdmin(AppUserRepository users, PasswordEncoder encoder) {
        return args -> users.findByEmailIgnoreCase("admin@nexushr.local").orElseGet(() -> {
            AppUser user = new AppUser();
            user.setEmail("admin@nexushr.local");
            user.setPasswordHash(encoder.encode("ChangeMe123!"));
            user.setRoles(Set.of(AppRole.ADMIN, AppRole.HR, AppRole.PAYROLL));
            return users.save(user);
        });
    }
}
