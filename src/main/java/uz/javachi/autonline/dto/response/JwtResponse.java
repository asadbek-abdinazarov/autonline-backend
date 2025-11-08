package uz.javachi.autonline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String phoneNumber;
    private List<String> roles;
    private String subscription;
    private List<String> subscriptionPermissions;
    private List<String> rolePermissions;
    private Boolean isActive;
}
