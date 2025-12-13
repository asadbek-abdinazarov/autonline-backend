package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponseDTO implements Serializable {

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
}
