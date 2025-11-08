package uz.javachi.autonline.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.javachi.autonline.model.UserSession;
import uz.javachi.autonline.repository.UserSessionRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionCleanup {

    private final UserSessionRepository repo;

    @Scheduled(fixedDelayString = "PT1H")
    public void cleanInactive() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(20);
        List<UserSession> old = repo.findAll().stream()
                .filter(s -> s.getLastActive().isBefore(cutoff) && "ACTIVE".equals(s.getStatus()))
                .toList();

        old.forEach(s -> {
            s.setStatus("LOGGED_OUT");
            repo.save(s);
        });
    }

    @Scheduled(fixedDelayString = "P3D")
    public void deleteRevoked() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(3);
        List<UserSession> toDelete = repo.findAll().stream()
                .filter(s -> s.getLastActive().isBefore(cutoff) && "REVOKED".equals(s.getStatus()))
                .toList();

        repo.deleteAll(toDelete);
    }
}
