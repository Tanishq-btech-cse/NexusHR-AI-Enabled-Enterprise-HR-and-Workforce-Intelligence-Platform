package com.nexushr.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.StringJoiner;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://nexus-hr-ai-enabled-enterprise-hr-a.vercel.app",
        "https://nexus-hr-ai-enabled-enterprise-hr-and-workforce-inte-hujql098n.vercel.app"
})
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AppUserRepository users;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, AppUserRepository users, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUser user = users.findByEmailIgnoreCase(request.email()).orElseThrow();
        String token = jwtService.issue(user);
        return new TokenResponse(token, "Bearer");
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType) {
    }
}