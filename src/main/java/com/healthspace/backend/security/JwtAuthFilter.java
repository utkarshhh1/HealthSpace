package com.healthspace.backend.security;

import com.healthspace.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // Nothing to do — continue filter chain. Downstream endpoints will enforce auth if required.
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            if (jwt == null || jwt.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            final String userEmail;
            try {
                userEmail = jwtUtil.extractUsername(jwt);
            } catch (Exception ex) {
                LOGGER.warn("Failed to extract username from JWT: {}", ex.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                try {
                    userDetails = this.userService.loadUserByUsername(userEmail);
                } catch (Exception ex) {
                    LOGGER.warn("User lookup failed for '{}': {}", userEmail, ex.getMessage());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Validate token against the loaded user
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    LOGGER.debug("Authenticated user '{}' with authorities: {}", userEmail, userDetails.getAuthorities());
                } else {
                    LOGGER.warn("Invalid JWT token for user '{}'", userEmail);
                }
            }

        } catch (Exception e) {
            // Log but don't stop request chain; downstream will return 401/403 as needed.
            LOGGER.error("Unexpected error in JwtAuthFilter: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
