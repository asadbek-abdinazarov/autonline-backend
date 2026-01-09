package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;
import uz.javachi.autonline.model.Subscription;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentsResponseToTeacherDTO {
    private Integer userId;

    private String username;

    private String fullName;

    private String phoneNumber;

    private Boolean isActive;

    private LocalDateTime nextPaymentDate;

    private SimpleSubscriptionResponseDTO subscription;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
