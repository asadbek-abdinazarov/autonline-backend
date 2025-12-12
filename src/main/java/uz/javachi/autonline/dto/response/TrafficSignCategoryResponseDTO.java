package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrafficSignCategoryResponseDTO {
    private Integer trafficSignsCategoriesId;
    private String name;
    private String icon;
    private String description;
}
