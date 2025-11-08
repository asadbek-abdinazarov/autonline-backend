package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.javachi.autonline.model.UserSession;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserIdAndStatus(Integer userId, String status);
    Optional<UserSession> findBySessionId(String sessionId);
    void deleteBySessionId(String sessionId);
}