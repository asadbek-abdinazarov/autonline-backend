package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class SubscriptionResponseDTO {
    private Integer subscriptionId;
    private String name;
    private String subscriptionDefName;
    private String description;
    private Double price;
    private Double discountedPrice;
    private String buyText;
    private List<String> features;
    private Boolean isActive;
    private Byte orderIndex;
    private Boolean isPopular;
    private Set<PermissionResponseDTO> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
