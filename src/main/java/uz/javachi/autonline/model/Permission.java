package uz.javachi.autonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uz.javachi.autonline.dto.response.PermissionResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.javachi.autonline.model.Role.rolesToDto;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    @NotBlank(message = "Permission name is required")
    @Size(min = 2, max = 100, message = "Permission name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(name = "description", length = 200)
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Business methods
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public static PermissionResponseDTO permissionToDto(Permission permission) {
        return PermissionResponseDTO.builder()
                .permissionId(permission.getPermissionId())
                .description(permission.getDescription())
                .name(permission.getName())
                .isActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .deletedAt(permission.getDeletedAt())
//                .roles(permission.getRoles().stream().map(Role::rolesToDto).collect(Collectors.toSet()))
                .build();
    }
}
