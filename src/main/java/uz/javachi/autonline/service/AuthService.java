package uz.javachi.autonline.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.repository.PermissionRepository;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    private final SessionService sessionService;
    private final MessageService messageService;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest httpReq) {

        User user = userRepository.findByUsernameAndSubscription(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageService.get("user.not.found.with.sm", loginRequest.getUsername())
                ));

        if (user.isAccountActive()) {
            throw new UserIsNotActiveException(messageService.get("user.is.blocked"));
        }

        List<UserSession> active = sessionService.listActiveSessions(user.getUserId());
        int MAX = 1;
        if (active.size() >= MAX) {
            active.stream().min(Comparator.comparing(UserSession::getCreatedAt)).ifPresent(oldest -> sessionService.revokeSession(oldest.getSessionId()));
        }

        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String sessionId = sessionService.createSession(user.getUserId(), ip, ua);

        Authentication authentication = authenticateCredentials(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal(),
                Map.of("sessionId", sessionId)
        );

        Subscription subscription = user.getSubscription();
        List<@NotBlank @Size(min = 2, max = 100) String> subscriptionPermissions =
                getActivePermissionNames(subscription);

        List<String> roles = getRoles(user);
        List<String> rolePermissions = getPermissions(user);
        return buildJwtResponse(jwtToken, user, subscription, subscriptionPermissions, rolePermissions, roles, sessionId);
    }


    private Authentication authenticateCredentials(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException(messageService.get("username.or.password.incorrect"), ex);
        }
    }

    @Transactional
    public JwtResponse registerUser(RegisterRequest registerRequest, HttpServletRequest httpReq) {

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException(messageService.get("password.do.not.match"));
        }

        validateUniqueUser(registerRequest);

        Subscription freeSubscription = getSubscriptionOrThrow();
        Role userRole = getRoleOrThrow();

        User newUser = buildNewUser(registerRequest, freeSubscription, userRole, passwordEncoder);
        newUser.setNextPaymentDate(LocalDateTime.now().plusDays(7));
        userRepository.save(newUser);

        List<String> activePermissions = getActivePermissionNames(freeSubscription);

        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String sessionId = sessionService.createSession(newUser.getUserId(), ip, ua);

        Authentication authentication = authenticateCredentials(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal(),
                Map.of("sessionId", sessionId)

        );

        List<String> roles = getRoles(newUser);
        List<String> rolePermissions = getPermissions(newUser);

        log.info("✅ Foydalanuvchi muvaffaqiyatli ro‘yxatdan o‘tdi: {}", newUser.getUsername());

        return buildJwtResponse(jwtToken, newUser, freeSubscription, activePermissions, rolePermissions, roles, sessionId);
    }
    @SuppressWarnings("unused")
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
            throw new UserAlreadyExistsException(messageService.get("username.already.exists"));
        }

        if (userRepository.existsByPhoneNumberAndNotDeleted(registerRequest.getPhoneNumber())) {
            throw new UserAlreadyExistsException(messageService.get("phone.number.already.exists"));
        }
    }

    private Subscription getSubscriptionOrThrow() {
        return subscriptionService.findByName(DEFAULT_SUBSCRIPTION)
                .orElseThrow(() -> new ResourceNotFoundException("Obuna topilmadi: %s".formatted(DEFAULT_SUBSCRIPTION)));
    }

    private Role getRoleOrThrow() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new CustomRoleNotFoundException("Rol topilmadi: %s".formatted(DEFAULT_ROLE)));
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
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: %s".formatted(permissionName)));
            role.addPermission(permission);
        });

        roleRepository.save(role);
        log.info("🟢 Created role: {} with permissions: {}", name, permissionNames);
    }

    public String logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return HttpStatus.BAD_REQUEST.name();
        String token = authHeader.substring(7);
        String sessionId = jwtUtils.extractSessionId(token);
        sessionService.logoutSession(sessionId);
        return HttpStatus.OK.name();
    }
}