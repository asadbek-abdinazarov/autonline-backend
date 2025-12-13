package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.NewsRequestDTO;
import uz.javachi.autonline.dto.request.UpdateUserRequestDTO;
import uz.javachi.autonline.dto.response.RoleResponseDTO;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;
import uz.javachi.autonline.dto.response.UserResponseDTO;
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.repository.NewsRepository;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.SubscriptionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uz.javachi.autonline.DefaultValues.DEFAULT_USER_ROLE;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RoleRepository roleRepository;
    private final UserBlockService userBlockService;
    private final NewsRepository newsRepository;
    private final MessageService messageService;
    private final StorageService storageService;


    @Transactional(readOnly = true)

    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException(messageService.get("current.user.not.authenticated")));

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
            return messageService.get("user.already.has.subscription", subscription.getName());
        }

        boolean updated = userRepository.updateUserSubscription(userId, subscriptionId) > 0;
        return updated ? messageService.get("user.subscription.success.updated") :
                messageService.get("user.subscription.not.updated");
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
            throw new IllegalStateException(messageService.get("user.already.has.role", user.getUsername(), role.getName()));
        }

        String isFree = subscriptionRepository.isUserHaveSubscription(userId);
        if (isFree.equals("FREE")) {
            throw new IllegalStateException(messageService.get("user.with.free.subscription.cannot.add.role"));
        }

        user.getRoles().add(role);
        userRepository.saveAndFlush(user);
        return messageService.get("role.success.added.to.user", role.getName(), user.getUsername());
    }

    @Transactional
    public String deleteRoleFromUser(Integer userId, Integer roleId) {
        Integer currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Current user is not authenticated!"));
        if (currentUserId.equals(userId)) {
            throw new IllegalStateException(messageService.get("user.cannot.remove.role.from.himself"));
        }
        User user = getUserOrThrow(userId);
        validateUserActive(user);

        Role role = getRoleOrThrow(roleId);
        validateRoleActive(role);

        if (role.getName().equals(DEFAULT_USER_ROLE)) {
            throw new RuntimeException(messageService.get("default.user.role.cant.remove", DEFAULT_USER_ROLE));
        }

        if (!userHasRole(user, roleId)) {
            throw new IllegalStateException(messageService.get("user.does.not.have.role", user.getUsername(), role.getName()));
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        return messageService.get("role.successful.deleted.from.user", role.getName(), user.getUsername());
    }


    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(messageService.get("user.not.found.with.id", userId)));
    }

    private Role getRoleOrThrow(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException(messageService.get("role.not.found.with.id", roleId)));
    }

    private Subscription getSubscriptionOrThrow(Integer subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(messageService.get("subscription.not.found.with.id", subscriptionId)));
    }

    private void validateUserActive(User user) {
        if (user.isAccountActive()) {
            throw new IllegalStateException(messageService.get("user.is.not.active", user.getUsername()));
        }
    }

    private void validateRoleActive(Role role) {
        if (Boolean.FALSE.equals(role.getIsActive()) || role.getDeletedAt() != null) {
            throw new IllegalStateException(messageService.get("role.is.not.active", role.getName()));
        }
    }

    private void validateSubscriptionActive(Subscription subscription) {
        if (Boolean.FALSE.equals(subscription.getIsActive())) {
            throw new IllegalStateException(messageService.get("subscription.is.not.active", subscription.getName()));
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
                .orElseThrow(() -> new RuntimeException(messageService.get("user.not.found.with.id", id)));

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
        return messageService.get("user.partial.updated", user.getUsername());
    }

    public String unblockUser(Integer userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException(messageService.get("user.not.found.with.id", userId)));
        if (!user.isAccountActive()) {
            throw new IllegalStateException(messageService.get("user.is.already.active"));
        }

        user.setIsActive(true);
        userRepository.saveAndFlush(user);

        return messageService.get("user.successful.unblocked", userId);
    }

    public String blockUser(Integer userId) {
        userBlockService.blockUser(userId);
        return messageService.get("user.successful.blocked", userId);
    }

    public String createNews(NewsRequestDTO dto) {
        News entity = News.toEntity(dto);
        entity.setNewsCreatedAt(LocalDateTime.now());

        if (dto.getNewsPhoto() != null && !dto.getNewsPhoto().isEmpty()) {
            try {
                String photo = storageService.uploadFile(dto.getNewsPhoto(), "images/news").get();
                entity.setNewsPhoto(photo);
                newsRepository.save(entity);
                return messageService.get("news.successfully.created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return messageService.get("news.not.successfully.created");
    }
}
