package com.nexushr.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
    private final EmailService emailService;

    public PasswordResetController(AppUserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // We use .orElse(null) to prevent leaking which emails exist in the database (Security Best Practice)
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            // Delete any old unused tokens for this user
            tokenRepository.deleteByUser(user);

            // Generate secure token
            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(token, user));

            // Format the frontend link (Make sure this matches your Vercel URL in production!)
            String resetLink = "https://nexus-hr-ai-enabled-enterprise-hr-and-workforce-inte-q69s6soby.vercel.app/reset-password?token=" + token;

            // Send the email
            emailService.sendResetEmail(user.getEmail(), resetLink);
        }

        // Always return success so hackers can't "guess" emails by looking for errors
        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has expired. Please request a new one.");
        }

        // Hash and save the new password
        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token so it cannot be reused
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password successfully reset."));
    }
}