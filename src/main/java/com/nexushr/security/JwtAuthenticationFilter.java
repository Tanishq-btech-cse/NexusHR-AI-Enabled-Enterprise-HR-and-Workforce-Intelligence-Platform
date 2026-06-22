package com.nexushr.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("🚨 INCOMING AUTHORIZATION HEADER: " + header); // 🔍 Watch this in Render Logs!

        if (header != null) {
            String token = null;

            // Handle both standard Bearer prefix and raw token strings safely
            if (header.startsWith("Bearer ")) {
                token = header.substring(7);
            } else if (!header.trim().isEmpty()) {
                token = header.trim();
            }

            if (token != null) {
                try {
                    Claims claims = jwtService.parse(token);
                    List<?> roles = claims.get("roles", List.class);

                    if (roles != null) {
                        var authorities = roles.stream()
                                .map(Object::toString)
                                .map(role -> {
                                    // Protect against double ROLE_ prefixing
                                    return role.startsWith("ROLE_") ?
                                            new SimpleGrantedAuthority(role) :
                                            new SimpleGrantedAuthority("ROLE_" + role);
                                })
                                .toList();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ SecurityContext successfully set for user: " + claims.getSubject() + " with roles: " + authorities);
                    }
                } catch (Exception e) {
                    System.out.println("❌ JWT Processing failed: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}