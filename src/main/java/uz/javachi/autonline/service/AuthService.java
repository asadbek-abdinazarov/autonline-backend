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
import uz.javachi.autonline.exceptions.CustomRoleNotFoundException;
import uz.javachi.autonline.exceptions.ResourceNotFoundException;
import uz.javachi.autonline.exceptions.UserAlreadyExistsException;
import uz.javachi.autonline.exceptions.UserIsNotActiveException;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.PermissionRepository;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.UserRepository;

import java.util.HashSet;
import java.util.List;

import static uz.javachi.autonline.DefaultValues.DEFAULT_ROLE;
import static uz.javachi.autonline.DefaultValues.DEFAULT_SUBSCRIPTION;
import static uz.javachi.autonline.utils.Utils.*;

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

        List<String> roles = getRoles(user);
        List<String> rolePermissions = getPermissions(user);
        return buildJwtResponse(jwtToken, user, subscription, subscriptionPermissions, rolePermissions, roles);
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

        User newUser = buildNewUser(registerRequest, freeSubscription, userRole, passwordEncoder);
        userRepository.save(newUser);

        List<String> activePermissions = getActivePermissionNames(freeSubscription);

        Authentication authentication = authenticateCredentials(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()
        );

        List<String> roles = getRoles(newUser);
        List<String> rolePermissions = getPermissions(newUser);

        log.info("✅ Foydalanuvchi muvaffaqiyatli ro‘yxatdan o‘tdi: {}", newUser.getUsername());

        return buildJwtResponse(jwtToken, newUser, freeSubscription, activePermissions, rolePermissions, roles);
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