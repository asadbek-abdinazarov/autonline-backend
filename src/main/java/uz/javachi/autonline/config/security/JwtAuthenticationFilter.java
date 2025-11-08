package uz.javachi.autonline.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, @Qualifier("customUserDetailsServiceIml") UserDetailsService userDetailsService, SessionService sessionService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            log.debug("JWT token found: {}", jwt != null ? "YES" : "NO");
            
            if (jwt != null) {
                log.debug("Validating JWT token...");
                boolean isValid = jwtUtils.validateToken(jwt);
                log.debug("JWT token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtils.extractUsername(jwt);
                    log.debug("Extracted username from JWT: {}", username);

                    String sessionId = jwtUtils.extractSessionId(jwt);

                    if (!sessionService.isActive(sessionId)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("User details loaded: {}", userDetails.getUsername());

                    if (userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        sessionService.updateLastActive(sessionId);

                        log.debug("Authentication set in SecurityContext for user: {}", username);
                    } else {
                        log.warn("User {} is disabled, authentication failed", username);
                    }
                } else {
                    log.warn("JWT token validation failed");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
