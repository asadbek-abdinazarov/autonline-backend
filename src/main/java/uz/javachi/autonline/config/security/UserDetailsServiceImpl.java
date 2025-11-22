package uz.javachi.autonline.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.exceptions.UserBlockedOrDeletedException;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.service.MessageService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import static uz.javachi.autonline.config.security.CustomUserDetails.getGrantedAuthorities;

@Service("customUserDetailsServiceIml")
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        if (user.isAccountActive()) {
            String fMessage = "User %s is inactive or deleted!".formatted(username);
            log.warn(fMessage);
            throw new UserBlockedOrDeletedException(fMessage);
        }

        if (user.getNextPaymentDate() != null && user.getNextPaymentDate().isBefore(LocalDateTime.now())) {
            user.setIsActive(false);
            userRepository.save(user);

            throw new UserBlockedOrDeletedException(messageService.get("subscription.is.expire"));
        }
        log.debug("Found user by ID: {} -> Username: {}", user.getUserId(), user.getUsername());
        return CustomUserDetails.fromUser(user);
    }

    public UserDetails loadUserById(Integer id) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", id);
        User user = userRepository.findByIdWithRoles(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id: " + id)
        );
        if (user.isAccountActive()) {
            log.warn("User {} is inactive or deleted", user.getUsername());
            throw new UsernameNotFoundException("Invalid credentials");
        }
        log.debug("Found user by ID: {} -> Username: {}", id, user.getUsername());
        return CustomUserDetails.fromUser(user);
    }

    @SuppressWarnings("unused")
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return getGrantedAuthorities(roles);
    }
}
