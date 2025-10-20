package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Permission;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.name = :name AND p.isActive = true AND p.deletedAt IS NULL")
    Optional<Permission> findActiveByName(@Param("name") String name);

    @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.deletedAt IS NULL")
    List<Permission> findAllActive();

    boolean existsByName(String name);
}
