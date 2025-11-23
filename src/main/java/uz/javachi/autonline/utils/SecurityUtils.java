package uz.javachi.autonline.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.javachi.autonline.config.security.CustomUserDetails;

import java.util.Optional;

@Slf4j
public class SecurityUtils {

    // Thread-local cache for Authentication to avoid repeated SecurityContext lookups
    private static final ThreadLocal<Authentication> AUTH_CACHE = new ThreadLocal<>();

    /**
     * Get the current authentication from SecurityContext (with caching)
     */
    private static Authentication getAuthentication() {
        Authentication cached = AUTH_CACHE.get();
        if (cached != null) {
            // Verify it's still the same (SecurityContext might have changed)
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            if (cached == current) {
                return cached;
            }
            // Context changed, update cache
            AUTH_CACHE.set(current);
            return current;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AUTH_CACHE.set(auth);
        return auth;
    }

    /**
     * Clear the authentication cache (call this when done with request processing)
     */
    public static void clearAuthCache() {
        AUTH_CACHE.remove();
    }

    /**
     * Get the current user ID from the authentication context
     *
     * @return Optional containing the user ID if available
     */
    public static Optional<Integer> getCurrentUserId() {
        Authentication authentication = getAuthentication();

        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (log.isDebugEnabled()) {
                log.debug("Found CustomUserDetails with ID: {}", userDetails.getUserId());
            }
            return Optional.of(userDetails.getUserId());
        }

        if (log.isDebugEnabled()) {
            log.debug("No valid user ID found in authentication context");
        }
        return Optional.empty();
    }


    /**
     * Get the current user ID from the authentication context
     *
     * @return the user ID
     * @throws IllegalStateException if no user ID is found
     */
    public static Integer getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    /**
     * Get the current username from the authentication context
     *
     * @return Optional containing the username if available
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }

        return Optional.empty();
    }

    /**
     * Get the current CustomUserDetails from the authentication context
     *
     * @return Optional containing the CustomUserDetails if available
     */
    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (log.isDebugEnabled()) {
                log.debug("Found CustomUserDetails: {}", userDetails.getUsername());
            }
            return Optional.of(userDetails);
        }

        if (log.isDebugEnabled()) {
            log.debug("No CustomUserDetails found in authentication context");
        }
        return Optional.empty();
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String roleAuthority = "ROLE_" + role.toUpperCase();
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(roleAuthority));
        }

        return false;
    }

    /**
     * Check if the current user is an admin
     *
     * @return true if the user has ADMIN role
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is a customer
     *
     * @return true if the user has CUSTOMER role
     */
    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    /**
     * Check if the current user is a moderator
     *
     * @return true if the user has MODERATOR role
     */
    public static boolean isModerator() {
        return hasRole("MODERATOR");
    }
} 