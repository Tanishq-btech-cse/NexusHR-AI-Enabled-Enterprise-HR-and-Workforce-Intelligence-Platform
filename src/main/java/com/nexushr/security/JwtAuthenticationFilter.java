package com.nexushr.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

    // 🌟 ADDED: Bypasses logging and processing entirely for actuator metrics/health probes
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();

        org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

        // Check both paths against the broad actuator wildcard pattern
        return pathMatcher.match("/actuator/**", servletPath)
                || pathMatcher.match("/actuator/**", requestUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parse(token);

                System.out.println("=================================");
                System.out.println("JWT SUBJECT : " + claims.getSubject());
                System.out.println("JWT ROLES   : " + claims.get("roles"));
                System.out.println("REQUEST URI : " + request.getRequestURI());
                System.out.println("=================================");

                List<?> roles = claims.get("roles", List.class);

                if (roles != null) {
                    var authorities = roles.stream()
                            .map(Object::toString)
                            .map(role -> role.startsWith("ROLE_")
                                    ? new SimpleGrantedAuthority(role)
                                    : new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();

                    System.out.println("AUTHORITIES : " + authorities);

                    UserDetails principal = User.withUsername(claims.getSubject())
                            .password("")
                            .authorities(authorities)
                            .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}