package uz.javachi.autonline.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.exceptions.UserBlockedOrDeletedException;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.service.MessageService;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service("customUserDetailsServiceIml")
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Value("${app.userdetails.cache.enabled:false}")
    private boolean cacheEnabled;

    @Value("${app.userdetails.cache.ttl:300000}")
    private long cacheTtlMs; // 5 minutes default

    // Simple in-memory cache with TTL
    private final ConcurrentMap<String, CachedUserDetails> userDetailsCache = new ConcurrentHashMap<>();

    private static class CachedUserDetails {
        final UserDetails userDetails;
        final long timestamp;

        CachedUserDetails(UserDetails userDetails) {
            this.userDetails = userDetails;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMs) {
            return System.currentTimeMillis() - timestamp > ttlMs;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (debugLogging()) {
            log.debug("Loading user by username: {}", username);
        }

        // Check cache first
        if (cacheEnabled) {
            CachedUserDetails cached = userDetailsCache.get(username);
            if (cached != null && !cached.isExpired(cacheTtlMs)) {
                if (debugLogging()) {
                    log.debug("User details found in cache for: {}", username);
                }
                return cached.userDetails;
            }
        }

        // Load from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        Hibernate.initialize(user.getRoles());
        user.getRoles().forEach(role -> Hibernate.initialize(role.getPermissions()));

        Hibernate.initialize(user.getSubscription());
        if (user.getSubscription() != null) {
            Hibernate.initialize(user.getSubscription().getPermissions());
        }

        // Check if user is active and not deleted
        if (!user.getIsActive() || user.isDeleted()) {
            String fMessage = "User %s is inactive or deleted!".formatted(username);
            log.warn(fMessage);
            // Remove from cache if exists
            userDetailsCache.remove(username);
            throw new UserBlockedOrDeletedException(fMessage);
        }

        // Check subscription expiration (optimized - only update if expired)
        if (user.getNextPaymentDate() != null && user.getNextPaymentDate().isBefore(LocalDateTime.now())) {
            // Only update if not already inactive
            if (user.getIsActive()) {
                user.setIsActive(false);
                userRepository.save(user);
            }
            // Remove from cache
            userDetailsCache.remove(username);
            throw new UserBlockedOrDeletedException(messageService.get("subscription.is.expire"));
        }

        // Create UserDetails
        UserDetails userDetails = CustomUserDetails.fromUser(user);

        // Cache the result
        if (cacheEnabled) {
            userDetailsCache.put(username, new CachedUserDetails(userDetails));
            // Clean up expired entries periodically (simple cleanup)
            if (userDetailsCache.size() > 1000) {
                userDetailsCache.entrySet().removeIf(entry -> entry.getValue().isExpired(cacheTtlMs));
            }
        }

        if (debugLogging()) {
            log.debug("Found user by ID: {} -> Username: {}", user.getUserId(), user.getUsername());
        }

        return userDetails;
    }

    private boolean debugLogging() {
        return log.isDebugEnabled();
    }

    /**
     * Clears the user details cache (useful when user is updated)
     */
    public void clearCache(String username) {
        if (username != null) {
            userDetailsCache.remove(username);
        }
    }

    /**
     * Clears all cached user details
     */
    public void clearAllCache() {
        userDetailsCache.clear();
    }

}

