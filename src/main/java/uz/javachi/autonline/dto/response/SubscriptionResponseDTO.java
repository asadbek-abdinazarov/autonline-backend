package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class SubscriptionResponseDTO {

    private Integer subscriptionId;

    private String name;

    private Boolean isActive;

    private Set<PermissionResponseDTO> permissions;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
