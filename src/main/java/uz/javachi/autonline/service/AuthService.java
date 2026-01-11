package uz.javachi.autonline.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.config.security.JwtUtils;
import uz.javachi.autonline.dto.request.LoginRequest;
import uz.javachi.autonline.dto.request.RefreshTokenRequest;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.dto.response.JwtResponse;
import uz.javachi.autonline.exceptions.*;
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.repository.PermissionRepository;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static uz.javachi.autonline.DefaultValues.DEFAULT_USER_ROLE;
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
    private final UserDetailsService userDetailsService;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest httpReq) {

        User user = userRepository.findByUsernameAndSubscription(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageService.get("user.not.found.with.sm", loginRequest.getUsername())
                ));

        // Fix: Check if user is NOT active or deleted
        if (!user.getIsActive() || user.isDeleted()) {
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

        Subscription subscription = user.getSubscription();
        List<String> roles = getRoles(user);

        List<String> permissions = new ArrayList<>(getPermissions(user));
        permissions.addAll(getActivePermissionNames(subscription));


        Result result = getResult(authentication, sessionId, roles, permissions);
        log.info("✅ Foydalanuvchi muvaffaqiyatli tizimga kirdi: {}", loginRequest.getUsername());
        return buildJwtResponse(result.accessToken(), result.refreshToken(), user, subscription, roles, permissions, sessionId);
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

        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String sessionId = sessionService.createSession(newUser.getUserId(), ip, ua);

        Authentication authentication = authenticateCredentials(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Pre-compute roles and permissions
        List<String> roles = getRoles(newUser);
        List<String> permissions = new ArrayList<>(getPermissions(newUser));
        permissions.addAll(getActivePermissionNames(freeSubscription));


        // Generate tokens with all claims in one go
        Result result = getResult(authentication, sessionId, roles, permissions);

        log.info("✅ Foydalanuvchi muvaffaqiyatli ro‘yxatdan o‘tdi: {}", newUser.getUsername());

        return buildJwtResponse(result.accessToken(), result.refreshToken(), newUser, freeSubscription, roles, permissions, sessionId);
    }

    private Result getResult(Authentication authentication, String sessionId, List<String> roles, List<String> permissions) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        String accessToken = jwtUtils.generateToken(userDetails, claims);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
        return new Result(accessToken, refreshToken);
    }

    private record Result(String accessToken, String refreshToken) {
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
        return roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseThrow(() -> new CustomRoleNotFoundException("Rol topilmadi: %s".formatted(DEFAULT_USER_ROLE)));
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

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest httpReq) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new TokenException(messageService.get("invalid.refresh.token"));
        }

        // Check if it's actually a refresh token
        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new TokenException(messageService.get("token.is.not.refresh.token"));
        }

        // Extract username from refresh token
        String username = jwtUtils.extractUsername(refreshToken);
        if (username == null) {
            throw new UsernameNotFoundException(messageService.get("user.not.found"));
        }

        // Load user
        User user = userRepository.findByUsernameAndSubscription(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageService.get("user.not.found.with.sm", username)
                ));

        // Check if user is active
        if (!user.getIsActive() || user.isDeleted()) {
            throw new UserIsNotActiveException(messageService.get("user.is.blocked"));
        }

        // Check subscription expiration
        if (user.getNextPaymentDate() != null && user.getNextPaymentDate().isBefore(LocalDateTime.now())) {
            if (user.getIsActive()) {
                user.setIsActive(false);
                userRepository.save(user);
            }
            throw new UserIsNotActiveException(messageService.get("subscription.is.expire"));
        }

        // Get or create session
        String sessionId;
        List<UserSession> activeSessions = sessionService.listActiveSessions(user.getUserId());

        if (activeSessions.isEmpty()) {
            // Create new session if no active session exists
            String ip = httpReq.getRemoteAddr();
            String ua = httpReq.getHeader("User-Agent");
            sessionId = sessionService.createSession(user.getUserId(), ip, ua);
        } else {
            // Use existing session and update last active
            UserSession activeSession = activeSessions.getFirst();
            sessionId = activeSession.getSessionId();
            sessionService.updateLastActive(sessionId);
        }

        // Load user details using UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Generate new tokens
        Subscription subscription = user.getSubscription();
        List<String> roles = getRoles(user);
        List<String> permissions = new ArrayList<>(getPermissions(user));
        permissions.addAll(getActivePermissionNames(subscription));

        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate new tokens
        Result result = getResult(authentication, sessionId, roles, permissions);

        log.debug("✅ Refresh token successful for user: {}", username);

        return buildJwtResponse(result.accessToken(), result.refreshToken(), user, subscription,
                permissions, roles, sessionId);
    }

    public String logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return HttpStatus.BAD_REQUEST.name();
        }

        try {
            String token = authHeader.substring(7).trim();
            if (!jwtUtils.validateToken(token)) {
                return HttpStatus.UNAUTHORIZED.name();
            }

            String sessionId = jwtUtils.extractSessionId(token);
            if (sessionId != null) {
                sessionService.logoutSession(sessionId);
            }

            SecurityContextHolder.clearContext();

            return HttpStatus.OK.name();
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return HttpStatus.INTERNAL_SERVER_ERROR.name();
        }
    }
}