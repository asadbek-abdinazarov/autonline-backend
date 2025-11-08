package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;
import uz.javachi.autonline.model.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PermissionResponseDTO {

    private Integer permissionId;

    private String name;

    private String description;

    private Boolean isActive;

//    private Set<RoleResponseDTO> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
