package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import uz.javachi.autonline.dto.request.TrafficSignsRequestDTO;
import uz.javachi.autonline.dto.response.TrafficSignsResponseDTO;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trafficSignsId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String photo;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traffic_signs_categories_id")
    private TrafficSignCategories trafficSignCategories;

    public static TrafficSigns toEntity(TrafficSignsRequestDTO dto) {
        return TrafficSigns.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive())
                .build();
    }

    public static TrafficSignsResponseDTO toDto(TrafficSigns entity) {
        return TrafficSignsResponseDTO.builder()
                .trafficSignsId(entity.getTrafficSignsId())
                .name(entity.getName())
                .description(entity.getDescription())
                .photo(entity.getPhoto())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
