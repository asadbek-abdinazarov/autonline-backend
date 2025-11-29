package uz.javachi.autonline.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.javachi.autonline.service.SessionService;

import java.io.IOException;
import java.util.Set;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    @Value("${app.logging.debug:false}")
    private boolean debugLogging;

    // Public endpoints that don't require authentication
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/",
            "/api/v1/public/",
            "/error",
            "/ws/",
            "/api/v1/news/",
            "/api/v1/statistic/",
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   @Qualifier("customUserDetailsServiceIml") UserDetailsService userDetailsService,
                                   SessionService sessionService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    /**
     * Checks if the request path is a public endpoint
     */
    private boolean isPublicPath(String path) {
        if (path == null) {
            return false;
        }
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Early exit for public endpoints
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);

            if (jwt == null) {
                if (debugLogging) {
                    log.debug("No JWT token found in request for path: {}", requestPath);
                }
                filterChain.doFilter(request, response);
                return;
            }

            // Skip refresh tokens - they should be handled by refresh endpoint
            if (jwtUtils.isRefreshToken(jwt)) {
                if (debugLogging) {
                    log.debug("Refresh token detected, skipping authentication filter");
                }
                filterChain.doFilter(request, response);
                return;
            }

            // Validate token first (fast check)
            if (!jwtUtils.validateToken(jwt)) {
                if (debugLogging) {
                    log.debug("JWT token validation failed for path: {}", requestPath);
                }
                // Check if token is specifically expired (not just invalid)
                if (jwtUtils.isTokenExpiredException(jwt)) {
                    // Set attribute to indicate token is expired
                    request.setAttribute("TOKEN_EXPIRED", true);
                    if (debugLogging) {
                        log.debug("Token is expired, setting TOKEN_EXPIRED attribute");
                    }
                }
                filterChain.doFilter(request, response);
                return;
            }

            // Extract session ID early to check session before loading user
            String sessionId = jwtUtils.extractSessionId(jwt);
            if (sessionId == null || !sessionService.isActive(sessionId)) {
                if (debugLogging) {
                    log.debug("Session is not active for sessionId: {}", sessionId);
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Extract username and load user details
            String username = jwtUtils.extractUsername(jwt);
            if (username == null) {
                if (debugLogging) {
                    log.debug("Could not extract username from token");
                }
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                log.warn("User {} is disabled, authentication failed", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Set authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Update session last active time (async if possible)
            sessionService.updateLastActive(sessionId);

            if (debugLogging) {
                log.debug("Authentication set in SecurityContext for user: {}", username);
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication for path {}: {}", requestPath, e.getMessage(), e);
            // Don't block the request, let SecurityConfig handle it
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            // Remove any whitespace
            return token.trim();
        }

        return null;
    }
}
