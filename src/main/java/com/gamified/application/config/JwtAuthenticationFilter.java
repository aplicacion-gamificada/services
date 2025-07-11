package com.gamified.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Profile("!test") // No se activa en perfil de test
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If no auth header or doesn't start with Bearer, continue chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (!isPublicEndpoint(request.getRequestURI())) {
                // Solo registra cuando no es un endpoint público
                logger.debug("No Authorization header found or invalid format for path: " + request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token and clean it
        String rawJwt = authHeader.substring(7);
        jwt = cleanToken(rawJwt);
        
        try {
            // Extract username from token
            username = jwtConfig.extractUsername(jwt);
            
            // If username exists and no authentication already in context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Validate token
                if (jwtConfig.validateToken(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Set details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("User authenticated: " + username);
                } else {
                    logger.debug("Invalid JWT token for user: " + username + " on path: " + request.getRequestURI());
                }
            }
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.error("JWT Signature validation failed for path: " + request.getRequestURI() + " - Token may be malformed or using wrong secret");
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            logger.error("JWT Token expired for path: " + request.getRequestURI() + " - User needs to login again");
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            logger.error("JWT Token malformed for path: " + request.getRequestURI() + " - Check token format");
        } catch (Exception ex) {
            logger.error("JWT Authentication error for path: " + request.getRequestURI() + " - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
    
    private String cleanToken(String rawToken) {
        String cleaned = rawToken.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
            logger.warn("Token contained quotes, cleaned: " + cleaned.substring(0, Math.min(20, cleaned.length())) + "...");
        }
        return cleaned;
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        // Quita el prefijo /api porque ya está incluido en el context-path
        return requestURI.startsWith("/auth/") || 
               requestURI.startsWith("/register/") ||
               requestURI.startsWith("/institutions") ||
               requestURI.startsWith("/api-docs/") ||
               requestURI.startsWith("/swagger-ui/");
    }
} 