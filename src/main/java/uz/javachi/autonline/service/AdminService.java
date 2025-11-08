package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.response.RoleResponseDTO;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;
import uz.javachi.autonline.dto.response.UpdateUserRequestDTO;
import uz.javachi.autonline.dto.response.UserResponseDTO;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.SubscriptionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

import static uz.javachi.autonline.DefaultValues.DEFAULT_ROLE;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RoleRepository roleRepository;


    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Current user is not authenticated!"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAllWithoutHimSelf(currentUserId, pageable);

        return userPage.map(this::mapToUserResponse);
    }

    @Transactional
    public String editUserSubscription(Integer userId, Integer subscriptionId) {
        User user = getUserOrThrow(userId);
        validateUserActive(user);

        Subscription subscription = getSubscriptionOrThrow(subscriptionId);
        validateSubscriptionActive(subscription);

        if (subscriptionId.equals(user.getSubscription().getSubscriptionId())) {
            return "User already has %s subscription".formatted(subscription.getName());
        }

        boolean updated = userRepository.updateUserSubscription(userId, subscriptionId) > 0;
        return updated ? "User subscription updated successfully!"
                : "User subscription not updated!";
    }


    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt", "updatedAt"))
                .stream()
                .map(Role::rolesToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(Subscription::subscriptionToDto)
                .toList();
    }

    @Transactional
    public String addRoleToUser(Integer userId, Integer roleId) {
        User user = getUserOrThrow(userId);
        validateUserActive(user);

        Role role = getRoleOrThrow(roleId);
        validateRoleActive(role);

        if (userHasRole(user, roleId)) {
            throw new IllegalStateException("User %s already has %s role!"
                    .formatted(user.getUsername(), role.getName()));
        }

        user.getRoles().add(role);
        userRepository.save(user);

        return "Role %s added to user %s successfully!"
                .formatted(role.getName(), user.getUsername());
    }

    @Transactional
    public String deleteRoleFromUser(Integer userId, Integer roleId) {
        Integer currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Current user is not authenticated!"));
        if (currentUserId.equals(userId)) {
            throw new IllegalStateException("User cannot remove role from himself!");
        }
        User user = getUserOrThrow(userId);
        validateUserActive(user);

        Role role = getRoleOrThrow(roleId);
        validateRoleActive(role);

        if (role.getName().equals(DEFAULT_ROLE)) {
            throw new RuntimeException("%s role is by default role you cannot remove from user!".formatted(DEFAULT_ROLE));
        }

        if (!userHasRole(user, roleId)) {
            throw new IllegalStateException("User %s does not have %s role!"
                    .formatted(user.getUsername(), role.getName()));
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        return "Role %s removed from user %s successfully!"
                .formatted(role.getName(), user.getUsername());
    }


    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: %s".formatted(userId)));
    }

    private Role getRoleOrThrow(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: %s".formatted(roleId)));
    }

    private Subscription getSubscriptionOrThrow(Integer subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: %s".formatted(subscriptionId)));
    }

    private void validateUserActive(User user) {
        if (user.isAccountActive()) {
            throw new IllegalStateException("User %s is not active!".formatted(user.getUsername()));
        }
    }

    private void validateRoleActive(Role role) {
        if (Boolean.FALSE.equals(role.getIsActive()) || role.getDeletedAt() != null) {
            throw new IllegalStateException("Role %s is not active!".formatted(role.getName()));
        }
    }

    private void validateSubscriptionActive(Subscription subscription) {
        if (Boolean.FALSE.equals(subscription.getIsActive())) {
            throw new IllegalStateException("%s subscription is not active!".formatted(subscription.getName()));
        }
    }

    private boolean userHasRole(User user, Integer roleId) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getRoleId().equals(roleId));
    }

    private UserResponseDTO mapToUserResponse(User user) {
        Subscription subscription = user.getSubscription();

        return UserResponseDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .nextPaymentDate(user.getNextPaymentDate())
                .roles(user.getRoles().stream().map(Role::rolesToDto).toList())
                .subscription(SubscriptionResponseDTO.builder()
                        .subscriptionId(subscription.getSubscriptionId())
                        .name(subscription.getName())
                        .permissions(subscription.getPermissions()
                                .stream()
                                .map(Permission::permissionToDto)
                                .collect(Collectors.toSet()))
                        .isActive(subscription.getIsActive())
                        .createdAt(subscription.getCreatedAt())
                        .updatedAt(subscription.getUpdatedAt())
                        .deletedAt(subscription.getDeletedAt())
                        .build())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Transactional
    public String partialUpdateUser(Integer id, UpdateUserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: %s".formatted(id)));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        userRepository.save(user);
        return "User updated successfully.";
    }
}
