package uz.javachi.autonline.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.javachi.autonline.config.security.CustomUserDetails;

import java.util.Optional;

@Slf4j
public class SecurityUtils {

    /**
     * Get the current user ID from the authentication context
     *
     * @return Optional containing the user ID if available
     */
    public static Optional<Integer> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Authentication object: {}", authentication);
        log.debug("Authentication principal: {}", authentication != null ? authentication.getPrincipal() : "null");
        log.debug("Authentication principal class: {}", authentication != null && authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            log.debug("Found CustomUserDetails with ID: {}", userDetails.getUserId());
            return Optional.of(userDetails.getUserId());
        }

        log.warn("No valid user ID found in authentication context. Authentication: {}, Principal: {}",
                authentication, authentication != null ? authentication.getPrincipal() : "null");
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Getting current user details. Authentication: {}, Principal: {}",
                authentication, authentication != null ? authentication.getPrincipal() : "null");

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            log.debug("Found CustomUserDetails: {}", authentication.getPrincipal());
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }

        log.warn("No CustomUserDetails found in authentication context");
        return Optional.empty();
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
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