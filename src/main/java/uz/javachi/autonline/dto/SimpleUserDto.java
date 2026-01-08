package uz.javachi.autonline.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDto {
    private Integer id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String subscription;
    private Boolean isActive;
    private LocalDateTime nextPaymentDate;
    private List<String> roles;
    private List<String> permissions;
}
