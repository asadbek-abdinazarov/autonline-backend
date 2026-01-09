package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.dto.response.SimpleSubscriptionResponseDTO;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;
import uz.javachi.autonline.service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SimpleSubscriptionResponseDTO>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getSubscriptions());
    }
}
