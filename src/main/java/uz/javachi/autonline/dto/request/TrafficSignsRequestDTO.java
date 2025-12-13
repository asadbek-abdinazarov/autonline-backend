package uz.javachi.autonline.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TrafficSignsRequestDTO {
    private String name;
    private String description;
    private Integer trafficSignCategoryId;
    private MultipartFile photo;
    private Boolean isActive;
}

