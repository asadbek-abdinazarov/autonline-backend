package uz.javachi.autonline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String subscription;
    private String sessionId;
    private Boolean isActive;
    private LocalDateTime nextPaymentDate;
    private List<String> roles;
    private List<String> rolePermissions;
    private List<String> subscriptionPermissions;
}
