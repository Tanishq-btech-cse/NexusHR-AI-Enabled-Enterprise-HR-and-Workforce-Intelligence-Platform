package com.nexushr.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Required by JPA
    public PasswordResetToken() {}

    public PasswordResetToken(String token, AppUser user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // Token expires in 15 mins
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public AppUser getUser() { return user; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
}