package com.nexushr.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {

    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    // private final EmailService emailService; // We will need this to actually send the email!

    public PasswordResetController(AppUserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 1. Find user (Don't throw an error if not found, to prevent email enumeration hacking)
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
        }

        // 2. Generate a secure random token
        String token = UUID.randomUUID().toString();

        // 3. Save it to the database
        tokenRepository.save(new PasswordResetToken(token, user));

        // 4. Send the email (You will implement this next)
        String resetLink = "https://nexushr.vercel.app/reset-password?token=" + token;
        // emailService.sendResetEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        // 1. Find the token in the database
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token."));

        // 2. Check if it is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has expired. Please request a new one.");
        }

        // 3. Hash the new password and save it
        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Delete the token so it can never be used again
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password successfully reset."));
    }
}