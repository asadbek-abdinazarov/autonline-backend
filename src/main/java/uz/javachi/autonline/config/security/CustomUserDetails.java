package uz.javachi.autonline.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    // Cached authorities to avoid recomputation
    private transient Collection<? extends GrantedAuthority> cachedAuthorities;

    public static CustomUserDetails fromUser(User user) {
        return CustomUserDetails.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nextPaymentDate(user.getNextPaymentDate())
                .isActive(user.getIsActive())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Lazy initialization with caching
        if (cachedAuthorities == null) {
            cachedAuthorities = getGrantedAuthorities(roles);
        }
        return cachedAuthorities;
    }

    /**
     * Invalidates the cached authorities (call this if roles are updated)
     */
    public void invalidateAuthoritiesCache() {
        this.cachedAuthorities = null;
    }

    static Collection<? extends GrantedAuthority> getGrantedAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Use streams for better performance and readability
        roles.stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .forEach(role -> {
                    // Add role authority
                    authorities.add(new SimpleGrantedAuthority("ROLE_%s".formatted(role.getName().toUpperCase())));
                    
                    // Add permission authorities
                    if (role.getPermissions() != null) {
                        role.getPermissions().stream()
                                .filter(permission -> permission.getIsActive() && !permission.isDeleted())
                                .forEach(permission -> 
                                    authorities.add(new SimpleGrantedAuthority(permission.getName().toUpperCase()))
                                );
                    }
                });
        
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
