package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.response.SubscriptionResponseDTO;
import uz.javachi.autonline.exceptions.ResourceNotFoundException;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.repository.SubscriptionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    public Optional<Subscription> findByName(String name) {
        return subscriptionRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getSubscriptions() {
        List<Subscription> sbdaia = subscriptionRepository.findSubscriptionByDeletedAtIsNullAndIsActive(true);
        if (sbdaia.isEmpty()) {
            throw new ResourceNotFoundException("Subscriptions are not found!");
        }
        return sbdaia.stream().map(Subscription::subscriptionToDto).toList();
    }
}
