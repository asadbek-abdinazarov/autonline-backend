package uz.javachi.autonline.dto.response;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String fullName;
    private String username;
    private String phoneNumber;
    private Boolean isActive;
}

