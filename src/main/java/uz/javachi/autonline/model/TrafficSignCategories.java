package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import uz.javachi.autonline.dto.response.TrafficSignCategoryResponseDTO;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrafficSignCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trafficSignsCategoriesId;
    private String name;
    private String icon;
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "trafficSignCategories")
    private List<TrafficSigns> trafficSigns;


    public static TrafficSignCategoryResponseDTO toDto(TrafficSignCategories entity) {
        return TrafficSignCategoryResponseDTO.builder()
                .trafficSignsCategoriesId(entity.getTrafficSignsCategoriesId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .description(entity.getDescription())
                .build();
    }

}
