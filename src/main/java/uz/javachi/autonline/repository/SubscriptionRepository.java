package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByName(String name);

    @Query(nativeQuery = true, value =
    """
                SELECT  * FROM subscription s
                WHERE s.deleted_at IS NULL AND s.is_active = :isActive
                AND name NOT LIKE 'STUDENT%' and name <> 'FREE'
                ORDER BY order_index
            """)
    List<Subscription> findSubscriptionByDeletedAtIsNullAndIsActive(Boolean isActive);

    @Query(nativeQuery = true, value = """
                SELECT s.name FROM user_subscriptions us
                            left join subscription s on us.subscription_id = s.subscription_id
                            where us.user_id = :userId and s.is_active = true
            """)
    String isUserHaveSubscription(@Param("userId") Integer userId);
}