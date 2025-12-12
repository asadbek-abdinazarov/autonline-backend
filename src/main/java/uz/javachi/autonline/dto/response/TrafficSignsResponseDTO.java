package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrafficSignsResponseDTO {
    private Integer trafficSignsId;

    private String name;

    private String description;

    private String photo;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
