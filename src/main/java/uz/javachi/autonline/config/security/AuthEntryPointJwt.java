package uz.javachi.autonline.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.error("Unauthorized error for path {}: {}", request.getServletPath(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("path", request.getServletPath());
        
        // Check for specific error conditions set by JwtAuthenticationFilter
        if (Boolean.TRUE.equals(request.getAttribute("TOKEN_MISSING"))) {
            body.put("error", "Unauthorized");
            body.put("message", "JWT token is missing. Please provide a valid token in the Authorization header.");
            body.put("code", "TOKEN_MISSING");
        } else if (Boolean.TRUE.equals(request.getAttribute("TOKEN_EXPIRED"))) {
            body.put("error", "Token Expired");
            body.put("message", "Access token has expired. Please refresh your token.");
            body.put("code", "TOKEN_EXPIRED");
        } else if (Boolean.TRUE.equals(request.getAttribute("TOKEN_INVALID"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Invalid JWT token. Please provide a valid token.");
            body.put("code", "TOKEN_INVALID");
        } else if (Boolean.TRUE.equals(request.getAttribute("TOKEN_INVALID_TYPE"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Invalid token type. Access token is required, not refresh token.");
            body.put("code", "TOKEN_INVALID_TYPE");
        } else if (Boolean.TRUE.equals(request.getAttribute("SESSION_INACTIVE"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Session is not active. Please login again.");
            body.put("code", "SESSION_INACTIVE");
        } else if (Boolean.TRUE.equals(request.getAttribute("SESSION_ID_MISSING"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Session ID is missing in token. Please login again.");
            body.put("code", "SESSION_ID_MISSING");
        } else if (Boolean.TRUE.equals(request.getAttribute("USERNAME_MISSING"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Username is missing in token. Please login again.");
            body.put("code", "USERNAME_MISSING");
        } else if (Boolean.TRUE.equals(request.getAttribute("USER_NOT_FOUND"))) {
            body.put("error", "Unauthorized");
            body.put("message", "User not found. Please login again.");
            body.put("code", "USER_NOT_FOUND");
        } else if (Boolean.TRUE.equals(request.getAttribute("USER_DISABLED"))) {
            body.put("error", "Unauthorized");
            body.put("message", "User account is disabled. Please contact administrator.");
            body.put("code", "USER_DISABLED");
        } else if (Boolean.TRUE.equals(request.getAttribute("USER_BLOCKED_OR_DELETED"))) {
            body.put("error", "Unauthorized");
            body.put("message", "User account is blocked or deleted. Please contact administrator.");
            body.put("code", "USER_BLOCKED_OR_DELETED");
        } else if (Boolean.TRUE.equals(request.getAttribute("AUTHENTICATION_ERROR"))) {
            body.put("error", "Unauthorized");
            body.put("message", "Authentication error occurred. Please try again.");
            body.put("code", "AUTHENTICATION_ERROR");
        } else {
            // Default error message
            body.put("error", "Unauthorized");
            body.put("message", authException.getMessage() != null && !authException.getMessage().isEmpty() 
                    ? authException.getMessage() 
                    : "Full authentication is required to access this resource");
            body.put("code", "UNAUTHORIZED");
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
