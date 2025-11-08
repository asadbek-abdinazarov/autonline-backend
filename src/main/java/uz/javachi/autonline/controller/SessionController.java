package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.model.UserSession;
import uz.javachi.autonline.service.SessionService;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/me")
    public ResponseEntity<List<UserSession>> mySessions() {
        Integer userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(sessionService.listActiveSessions(userId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> revoke(@PathVariable String sessionId) {

//        Integer userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("User not authenticated"));

        sessionService.revokeSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
