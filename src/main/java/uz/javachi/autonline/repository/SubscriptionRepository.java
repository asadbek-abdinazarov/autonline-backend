package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.javachi.autonline.model.Subscription;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByName(String name);
}