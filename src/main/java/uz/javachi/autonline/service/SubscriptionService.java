package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.repository.SubscriptionRepository;

import java.lang.ScopedValue;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    public Optional<Subscription> findByName(String name) {
        return subscriptionRepository.findByName(name);
    }
}
