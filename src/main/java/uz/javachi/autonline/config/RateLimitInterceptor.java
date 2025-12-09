package uz.javachi.autonline.config;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    private static final long BLOCK_DURATION_MS = Duration.ofMinutes(5).toMillis();

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(20)
                        .refillGreedy(20, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        // =============================
        // 1) CHECK IF IP IS BLOCKED
        // =============================
        Long blockedUntil = blockedIps.get(ip);

        if (blockedUntil != null) {
            if (blockedUntil > now) {
                log.warn("Blocked IP attempt: {} (blocked until {})", ip, blockedUntil);
                response.setStatus(429);
                response.getWriter().write("Your IP is temporarily blocked. Try again later.");
                return false;
            } else {
                blockedIps.remove(ip);
                log.info("IP block expired, removed: {}", ip);
            }
        }

        // =============================
        // 2) RATE LIMIT CHECK
        // =============================
        Bucket bucket = bucketCache.computeIfAbsent(ip, i -> createBucket());

        if (bucket.tryConsume(1)) {
            return true;
        }

        // =============================
        // 3) BLOCK THE IP
        // =============================
        long blockUntilTime = now + BLOCK_DURATION_MS;
        blockedIps.put(ip, blockUntilTime);

        log.error("Rate limit exceeded! Blocking IP: {} for {} minutes",
                ip,
                BLOCK_DURATION_MS / 1000 / 60
        );

        response.setStatus(429);
        response.getWriter().write("Too many requests. Your IP is temporarily blocked.");
        return false;
    }
}

