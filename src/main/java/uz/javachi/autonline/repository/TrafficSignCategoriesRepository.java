package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.javachi.autonline.model.TrafficSignCategories;

import java.util.List;

public interface TrafficSignCategoriesRepository extends JpaRepository<TrafficSignCategories, Integer> {
    @Query(nativeQuery = true, value = """
            SELECT * FROM traffic_sign_categories tsc ORDER BY traffic_signs_categories_id
            """)
    List<TrafficSignCategories> findAllTrafficSignCategories();

}