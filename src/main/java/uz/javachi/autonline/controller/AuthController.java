package uz.javachi.autonline.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.customAnnotations.Loggable;
import uz.javachi.autonline.dto.request.LoginRequest;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.dto.response.JwtResponse;
import uz.javachi.autonline.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"}, maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Loggable
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    @Loggable
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.registerUser(registerRequest));
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
