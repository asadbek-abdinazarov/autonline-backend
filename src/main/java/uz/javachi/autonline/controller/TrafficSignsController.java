package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.request.TrafficSignsRequestDTO;
import uz.javachi.autonline.dto.response.TrafficSignsResponseDTO;
import uz.javachi.autonline.service.TrafficSignsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traffic-signs")
@RequiredArgsConstructor
public class TrafficSignsController {

    private final TrafficSignsService trafficSignsService;

    @GetMapping("/category/{id}")
    @PreAuthorize("hasAuthority('VIEW_TRAFFIC_SIGNS') or hasRole('ADMIN')")
    public ResponseEntity<List<TrafficSignsResponseDTO>> getAllTrafficSignsByCategoryId(@PathVariable("id") Integer categoryId) {
        return ResponseEntity.ok(trafficSignsService.getAllTrafficSigns(categoryId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CREATE_TRAFFIC_SIGNS') or hasRole('ADMIN')")
    public ResponseEntity<TrafficSignsResponseDTO> createTrafficSign(
            @ModelAttribute TrafficSignsRequestDTO dto) {
        return ResponseEntity.ok(trafficSignsService.createTrafficSign(dto));
    }

}
