package uz.javachi.autonline.utils;

import org.springframework.security.crypto.password.PasswordEncoder;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.dto.response.JwtResponse;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Utils {
    public static JwtResponse buildJwtResponse(String token, User user, Subscription subscription, List<String> subscriptionPermissions, List<String> rolePermissions, List<String> roles, String sessionId) {
        return JwtResponse.builder()
                .id(user.getUserId())
                .token(token)
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .nextPaymentDate(user.getNextPaymentDate())
                .subscription(subscription.getName())
                .subscriptionPermissions(subscriptionPermissions)
                .sessionId(sessionId)
                .rolePermissions(rolePermissions)
                .roles(roles)
                .build();
    }

    public static User buildNewUser(RegisterRequest request, Subscription subscription, Role role, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .isActive(true)
                .subscription(subscription)
                .paymentHistory(new ArrayList<>())
                .roles(Set.of(role))
                .build();
    }

    public static List<String> getActivePermissionNames(Subscription subscription) {
        return subscription.getPermissions().stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()) && !permission.isDeleted())
                .map(Permission::getName)
                .toList();
    }

    public static List<String> getPermissions(User user) {
        return user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getIsActive() && !permission.isDeleted())
                .map(Permission::getName)
                .distinct()
                .toList();
    }

    public static List<String> getRoles(User user) {
        return user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .map(Role::getName)
                .toList();
    }
}
