package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponseDTO {

    private Integer id;

    private String username;

    private String fullName;

    private String phoneNumber;

    private Boolean isActive;

    private LocalDateTime nextPaymentDate;

    private SubscriptionResponseDTO subscription;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private List<RoleResponseDTO> roles;

    private List<PaymentHistoryResponseDTO> paymentHistory;

//    private List<PermissionResponseDTO> subscriptionPermissions;

//    private List<PermissionResponseDTO> rolePermissions;
}
