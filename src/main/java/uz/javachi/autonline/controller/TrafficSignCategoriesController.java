package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.dto.response.TrafficSignCategoryResponseDTO;
import uz.javachi.autonline.service.TrafficSignCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traffic-sign-categories")
@RequiredArgsConstructor
public class TrafficSignCategoriesController {

    private final TrafficSignCategoryService trafficSignCategoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_TRAFFIC_SIGNS') or hasRole('ADMIN')")
    public ResponseEntity<List<TrafficSignCategoryResponseDTO>> getAllTrafficSignCategories() {
        return ResponseEntity.ok(trafficSignCategoryService.getAllTrafficSignCategories());
    }
}
