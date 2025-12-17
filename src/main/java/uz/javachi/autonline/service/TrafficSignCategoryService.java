package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.response.TrafficSignCategoryResponseDTO;
import uz.javachi.autonline.model.TrafficSignCategories;
import uz.javachi.autonline.repository.TrafficSignCategoriesRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrafficSignCategoryService {

    private final TrafficSignCategoriesRepository trafficSignCategoriesRepository;

    public List<TrafficSignCategoryResponseDTO> getAllTrafficSignCategories() {
        return trafficSignCategoriesRepository.findAllTrafficSignCategories().stream()
                .map(TrafficSignCategories::toDto)
                .toList();
    }
}
