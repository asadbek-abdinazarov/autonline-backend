package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name = :name AND r.isActive = true AND r.deletedAt IS NULL")
    Optional<Role> findActiveByName(@Param("name") String name);

    @Query("SELECT r FROM Role r WHERE r.isActive = true AND r.deletedAt IS NULL")
    List<Role> findAllActive();

    boolean existsByName(String name);

    Optional<Role> findRoleByRoleIdAndDeletedAtIsNull(Integer roleId);
}
