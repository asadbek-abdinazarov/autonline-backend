package uz.javachi.autonline.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.model.UserSession;
import uz.javachi.autonline.repository.UserSessionRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final UserSessionRepository repo;

    @Transactional
    public String createSession(Integer userId, String ip, String ua) {
        String sessionId = UUID.randomUUID().toString();
        UserSession s = new UserSession();
        s.setUserId(userId);
        s.setSessionId(sessionId);
        s.setIpAddress(ip);
        s.setUserAgent(ua);
        s.setCreatedAt(OffsetDateTime.now());
        s.setLastActive(OffsetDateTime.now());
        s.setStatus("ACTIVE");
        repo.save(s);
        return sessionId;
    }

    @Transactional(readOnly = true)
    public boolean isActive(String sessionId) {
        return repo.findBySessionId(sessionId)
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .isPresent();
    }

    @Transactional
    public void revokeSession(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("REVOKED");
            s.setLastActive(OffsetDateTime.now());
            repo.save(s);
        });
    }

    @Transactional
    public void updateLastActive(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            s.setLastActive(OffsetDateTime.now());
            repo.save(s);
        });
    }

    public List<UserSession> listActiveSessions(Integer userId) {
        return repo.findByUserIdAndStatus(userId, "ACTIVE");
    }

    @Transactional
    public void logoutSession(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("LOGGED_OUT");
            s.setLastActive(OffsetDateTime.now());
            repo.save(s);
        });
    }
}
