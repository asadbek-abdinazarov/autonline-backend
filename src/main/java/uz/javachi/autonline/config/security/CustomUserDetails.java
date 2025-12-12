package uz.javachi.autonline.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Integer userId;
    private String username;
    private String password;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime nextPaymentDate;
    private Set<Role> roles;
    private Subscription subscriptions;

    private transient Collection<? extends GrantedAuthority> cachedAuthorities;

    public static CustomUserDetails fromUser(User user) {
        return CustomUserDetails.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nextPaymentDate(user.getNextPaymentDate())
                .isActive(user.getIsActive())
                .phoneNumber(user.getPhoneNumber())
                .subscriptions(user.getSubscription())
                .roles(user.getRoles())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (cachedAuthorities == null) {
            cachedAuthorities = getGrantedAuthorities(roles, subscriptions);
        }
        return cachedAuthorities;
    }

    /**
     * Invalidates the cached authorities (call this if roles are updated)
     */
    public void invalidateAuthoritiesCache() {
        this.cachedAuthorities = null;
    }

    static Collection<? extends GrantedAuthority> getGrantedAuthorities(Set<Role> roles,
                                                                        Subscription subscription) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        Set<Permission> allPermissions = new HashSet<>();

        if (roles != null) {
            roles.stream()
                    .filter(r -> r.getIsActive() && !r.isDeleted())
                    .forEach(role -> {

                        authorities.add(new SimpleGrantedAuthority(
                                "ROLE_%s".formatted(role.getName().toUpperCase())
                        ));

                        if (role.getPermissions() != null) {
                            allPermissions.addAll(
                                    role.getPermissions().stream()
                                            .filter(p -> p.getIsActive() && !p.isDeleted())
                                            .toList()
                            );
                        }
                    });
        }

        if (subscription != null) {
            Set<Permission> permissions = subscription.getPermissions();
            if (!permissions.isEmpty()) {
                allPermissions.addAll(
                        permissions.stream()
                                .filter(p -> p.getIsActive() && !p.isDeleted())
                                .toList()
                );
            }
        }

        allPermissions.forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName().toUpperCase()))
        );

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
