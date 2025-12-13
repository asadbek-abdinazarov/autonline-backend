package uz.javachi.autonline.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.request.TrafficSignsRequestDTO;
import uz.javachi.autonline.dto.response.TrafficSignsResponseDTO;
import uz.javachi.autonline.model.TrafficSignCategories;
import uz.javachi.autonline.model.TrafficSigns;
import uz.javachi.autonline.repository.TrafficSignCategoriesRepository;
import uz.javachi.autonline.repository.TrafficSignsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrafficSignsService {

    private final TrafficSignsRepository trafficSignsRepository;
    private final StorageService storageService;
    private final TrafficSignCategoriesRepository trafficSignCategoriesRepository;

    public List<TrafficSignsResponseDTO> getAllTrafficSigns(Integer categoryId) {
        List<TrafficSigns> allByCategoryIdAndIsActiveTrue = trafficSignsRepository.findAllByCategoryIdAndIsActiveTrue(categoryId, true);
        if (allByCategoryIdAndIsActiveTrue.isEmpty()) {
            return Collections.emptyList();
        }
        return allByCategoryIdAndIsActiveTrue.stream().map(TrafficSigns::toDto).toList();
    }

    public TrafficSignsResponseDTO createTrafficSign(TrafficSignsRequestDTO dto) {


        TrafficSigns entity = TrafficSigns.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());

        Optional<TrafficSignCategories> byId = trafficSignCategoriesRepository.findById(dto.getTrafficSignCategoryId());
        if (byId.isEmpty()) {
            throw new EntityNotFoundException("Traffic sign categories id not found");
        }
        TrafficSignCategories trafficSignCategories = byId.get();

        if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
            try {
                String photo = storageService.uploadFile(dto.getPhoto(), "images/traffic-signs").get();
                entity.setPhoto(photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        entity.setTrafficSignCategories(trafficSignCategories);
        trafficSignsRepository.save(entity);

        return TrafficSigns.toDto(entity);
    }

}
