package uz.javachi.autonline.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.config.security.JwtUtils;
import uz.javachi.autonline.dto.request.LoginRequest;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.dto.response.JwtResponse;
import uz.javachi.autonline.exceptions.*;
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.repository.*;

import java.util.*;

import static uz.javachi.autonline.DefaultValues.DEFAULT_ROLE;
import static uz.javachi.autonline.DefaultValues.DEFAULT_SUBSCRIPTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public JwtResponse authenticateUser(LoginRequest loginRequest) {

        User user = userRepository.findByUsernameAndSubscription(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("%s nomda foydalanuvchi topilmadi", loginRequest.getUsername())
                ));

        if (user.isAccountActive()) {
            throw new UserIsNotActiveException(
                    "Foydalanuvchi hisobingiz bloklangan. Iltimos, administrator bilan bog'laning."
            );
        }

        Authentication authentication = authenticateCredentials(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()
        );

        Subscription subscription = user.getSubscription();
        List<@NotBlank @Size(min = 2, max = 100) String> subscriptionPermissions =
                getActivePermissionNames(subscription);

        // ⚠️ Kommentdagi kodga tegilmadi:
        // Roles and permissions are now handled in JWT token generation
        // These variables are kept for future use if needed
//        @SuppressWarnings("unused")
        /*List<String> roles = user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .map(Role::getName)
                .toList();*/

//        @SuppressWarnings("unused")
       /* List<String> permissions = user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getIsActive() && !permission.isDeleted())
                .map(Permission::getName)
                .distinct()
                .toList();*/

        buildJwtResponse(jwtToken, user, subscription, subscriptionPermissions);

        return JwtResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .id(user.getUserId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .subscription(subscription.getName())
                .permissions(subscriptionPermissions)
                .isActive(user.getIsActive())
                .build();
    }

    private Authentication authenticateCredentials(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Foydalanuvchi nomi yoki parol noto‘g‘ri", ex);
        }
    }

    @Transactional
    public JwtResponse registerUser(RegisterRequest registerRequest) {

        validateUniqueUser(registerRequest);

        Subscription freeSubscription = getSubscriptionOrThrow();
        Role userRole = getRoleOrThrow();

        User newUser = buildNewUser(registerRequest, freeSubscription, userRole);
        userRepository.save(newUser);

        List<String> activePermissions = getActivePermissionNames(freeSubscription);

        Authentication authentication = authenticateCredentials(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()
        );

        log.info("✅ Foydalanuvchi muvaffaqiyatli ro‘yxatdan o‘tdi: {}", newUser.getUsername());

        return buildJwtResponse(jwtToken, newUser, freeSubscription, activePermissions);
    }

    @Transactional
    public void initializeDefaultRolesAndPermissions() {
        createPermissionIfNotExists("READ_LESSONS", "Read lessons permission");
        createPermissionIfNotExists("WRITE_LESSONS", "Write lessons permission");
        createPermissionIfNotExists("DELETE_LESSONS", "Delete lessons permission");
        createPermissionIfNotExists("MANAGE_USERS", "Manage users permission");
        createPermissionIfNotExists("VIEW_PAYMENTS", "View payments permission");
        createPermissionIfNotExists("MANAGE_PAYMENTS", "Manage payments permission");

        createRoleIfNotExists("USER", "Default user role", List.of("READ_LESSONS"));
        createRoleIfNotExists("ADMIN", "Administrator role", List.of(
                "READ_LESSONS", "WRITE_LESSONS", "DELETE_LESSONS",
                "MANAGE_USERS", "VIEW_PAYMENTS", "MANAGE_PAYMENTS"
        ));
    }

    private void validateUniqueUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsernameAndNotDeleted(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Bu nom bilan foydalanuvchi allaqachon mavjud!");
        }

        if (userRepository.existsByPhoneNumberAndNotDeleted(registerRequest.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Bu telefon raqami allaqachon ro‘yxatdan o‘tgan!");
        }
    }

    private Subscription getSubscriptionOrThrow() {
        return subscriptionService.findByName(DEFAULT_SUBSCRIPTION)
                .orElseThrow(() -> new ResourceNotFoundException(STR."Obuna topilmadi: \{DEFAULT_SUBSCRIPTION}"));
    }

    private Role getRoleOrThrow() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new CustomRoleNotFoundException(STR."Rol topilmadi: \{DEFAULT_ROLE}"));
    }

    private User buildNewUser(RegisterRequest request, Subscription subscription, Role role) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .subscription(subscription)
                .paymentHistory(new ArrayList<>()) // Mutable list
                .roles(Set.of(role))
                .build();
    }

    private List<String> getActivePermissionNames(Subscription subscription) {
        return subscription.getPermissions().stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()) && !permission.isDeleted())
                .map(Permission::getName)
                .toList();
    }

    private JwtResponse buildJwtResponse(String token, User user, Subscription subscription, List<String> permissions) {
        return JwtResponse.builder()
                .id(user.getUserId())
                .token(token)
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .subscription(subscription.getName())
                .permissions(permissions)
                .build();
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (permissionRepository.existsByName(name)) {
            return;
        }
        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build();
        permissionRepository.save(permission);
        log.info("🟢 Created permission: {}", name);
    }

    private void createRoleIfNotExists(String name, String description, List<String> permissionNames) {
        if (roleRepository.existsByName(name)) {
            return;
        }

        Role role = Role.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .permissions(new HashSet<>())
                .build();

        permissionNames.forEach(permissionName -> {
            Permission permission = permissionRepository.findActiveByName(permissionName)
                    .orElseThrow(() -> new ResourceNotFoundException(STR."Permission not found: \{permissionName}"));
            role.addPermission(permission);
        });

        roleRepository.save(role);
        log.info("🟢 Created role: {} with permissions: {}", name, permissionNames);
    }
}