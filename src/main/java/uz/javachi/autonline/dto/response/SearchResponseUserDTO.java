package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResponseUserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String phoneNumber;
    private Boolean isActive;
}
