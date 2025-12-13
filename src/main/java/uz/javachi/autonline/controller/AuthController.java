package uz.javachi.autonline.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.customAnnotations.Loggable;
import uz.javachi.autonline.dto.request.LoginRequest;
import uz.javachi.autonline.dto.request.RefreshTokenRequest;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "https://autonline-production.up.railway.app", "http://autonline.uz", "http://localhost:8080"}, maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Loggable
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest, httpReq));
    }

    @PostMapping("/register")
    @Loggable
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.registerUser(registerRequest, httpReq));
    }
    @PostMapping("/logout")
    @Loggable
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.logout(authHeader));
    }

    @PostMapping("/refresh-token")
    @Loggable
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest, HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest, httpReq));
    }
}

   /* @PostMapping("/init")
    public ResponseEntity<?> initializeDefaultData() {
        try {
            authService.initializeDefaultRolesAndPermissions();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Default roles and permissions initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to initialize default data", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Initialization failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }*/
