package uz.javachi.autonline.service;

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
import uz.javachi.autonline.exceptions.UserIsNotActiveException;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.PermissionRepository;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("%s nomda foydalanuvchi topilmadi".formatted(loginRequest.getUsername())));

        if (user.isAccountActive()) {
            throw new UserIsNotActiveException("Foydalanuvchi hisobingiz bloklangan. Iltimos, administrator bilan bog'laning.");
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Foydalanuvchi nomi yoki Parol noto'g'ri", ex);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());

        // Roles and permissions are now handled in JWT token generation
        // These variables are kept for future use if needed
        @SuppressWarnings("unused")
        List<String> roles = user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .map(Role::getName)
                .toList();

        @SuppressWarnings("unused")
        List<String> permissions = user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getIsActive() && !permission.isDeleted())
                .map(Permission::getName)
                .distinct()
                .toList();

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getUserId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .build();
    }

    @Transactional
    public String registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsernameAndNotDeleted(registerRequest.getUsername())) {
            throw new RuntimeException("Bu nom bilan foydalanuvchi allaqachon mavjud!");
        }

        if (userRepository.existsByPhoneNumberAndNotDeleted(registerRequest.getPhoneNumber())) {
            throw new RuntimeException("Bu telefon raqami allaqachon ro'yxatdan o'tgan!");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findActiveByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: USER role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role foundRole = roleRepository.findActiveByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                roles.add(foundRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());
        return "User registered successfully!";
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
                "MANAGE_USERS", "VIEW_PAYMENTS", "MANAGE_PAYMENTS"));
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = Permission.builder()
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .build();
            permissionRepository.save(permission);
            log.info("Created permission: {}", name);
        }
    }

    private void createRoleIfNotExists(String name, String description, List<String> permissionNames) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .permissions(new HashSet<>())
                    .build();

            permissionNames.forEach(permissionName -> {
                Permission permission = permissionRepository.findActiveByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
                role.addPermission(permission);
            });

            roleRepository.save(role);
            log.info("Created role: {} with permissions: {}", name, permissionNames);
        }
    }
}
