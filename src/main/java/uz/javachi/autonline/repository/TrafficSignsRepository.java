package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.javachi.autonline.model.TrafficSigns;

import java.util.List;

public interface TrafficSignsRepository extends JpaRepository<TrafficSigns, Integer> {
    @Query(nativeQuery = true, value = """
                SELECT ts.traffic_signs_id,
                       ts.name,
                       ts.description,
                       ts.photo,
                       ts.is_active,
                       ts.created_at,
                                   ts.deleted_at,
                                               ts.updated_at,
                       ts.traffic_signs_categories_id
                FROM traffic_signs ts
                LEFT JOIN traffic_sign_categories tsc
                       ON ts.traffic_signs_categories_id = tsc.traffic_signs_categories_id
                WHERE tsc.traffic_signs_categories_id = :categoryId
                  AND ts.is_active = :isActive
            """)
    List<TrafficSigns> findAllByCategoryIdAndIsActiveTrue(Integer categoryId, Boolean isActive);
}