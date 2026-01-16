package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleSubscriptionResponseDTO {
    private Integer id;
    private String name;
    private String defName;
    private String description;
    private Double price;
    private Double discountedPrice;
    private String buyText;
    private List<String> features;
    private Boolean isActive;
    private Byte orderIndex;
    private Boolean isPopular;
}
