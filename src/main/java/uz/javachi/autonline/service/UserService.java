package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;
import uz.javachi.autonline.dto.response.UserResponseDTO;
import uz.javachi.autonline.model.Permission;
import uz.javachi.autonline.model.Role;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public List<UserResponseDTO> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream().map(
                user -> {
                    Subscription subscription = user.getSubscription();
                    Set<Role> roles = user.getRoles();
                    return UserResponseDTO.builder()
                            .id(user.getUserId())
                            .username(user.getUsername())
                            .phoneNumber(user.getPhoneNumber())
                            .isActive(user.getIsActive())
                            .roles(
                                    roles.stream().map(Role::rolesToDto).toList()
                            )
                            .subscription(
                                    SubscriptionResponseDTO.builder()
                                            .subscriptionId(subscription.getSubscriptionId())
                                            .name(subscription.getName())
                                            .permissions(user.getSubscription().getPermissions()
                                                    .stream().map(Permission::permissionToDto).collect(Collectors.toSet()))
                                            .isActive(subscription.getIsActive())
                                            .createdAt(subscription.getCreatedAt())
                                            .updatedAt(subscription.getUpdatedAt())
                                            .deletedAt(subscription.getDeletedAt())
                                            .build()
                            )
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .deletedAt(user.getDeletedAt())
                            .build();
                }
        ).toList();
    }
}
