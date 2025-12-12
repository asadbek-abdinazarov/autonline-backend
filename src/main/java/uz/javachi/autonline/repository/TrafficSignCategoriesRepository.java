package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.javachi.autonline.model.TrafficSignCategories;

public interface TrafficSignCategoriesRepository extends JpaRepository<TrafficSignCategories, Integer> {
}