package uz.javachi.autonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @NotBlank(message = "User access name is required")
    @Size(min = 2, max = 50, message = "User access name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    private String defName;

    private String description;

    private Double price;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> features;

    @Column(name = "buy_text", columnDefinition = "TEXT")
    private String buyText;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    private Integer studentLimit;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "subscription_permissions",
            joinColumns = @JoinColumn(name = "subscription_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName) && permission.getIsActive());
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public static SubscriptionResponseDTO subscriptionToDto(Subscription subscription) {
        return SubscriptionResponseDTO.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .name(subscription.getName())
                .defName(subscription.getDefName())
                .features(subscription.getFeatures())
                .price(subscription.getPrice())
                .buyText(subscription.getBuyText())
                .description(subscription.getDescription())
                .permissions(subscription.getPermissions()
                        .stream().map(Permission::permissionToDto)
                        .collect(Collectors.toSet()))
                .isActive(subscription.getIsActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .deletedAt(subscription.getDeletedAt())
                .build();
    }
}
