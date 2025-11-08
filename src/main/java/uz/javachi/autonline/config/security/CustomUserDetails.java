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
        return getGrantedAuthorities(roles);
    }

    static Collection<? extends GrantedAuthority> getGrantedAuthorities(Set<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (Role role : roles) {
                if (role.getIsActive() && !role.isDeleted()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_%s".formatted(role.getName().toUpperCase())));
                    if (role.getPermissions() != null) {
                        for (Permission permission : role.getPermissions()) {
                            if (permission.getIsActive() && !permission.isDeleted()) {
                                authorities.add(new SimpleGrantedAuthority(permission.getName().toUpperCase()));
                            }
                        }
                    }
                }
            }
        }
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
