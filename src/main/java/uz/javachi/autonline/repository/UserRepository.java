package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query(value = """
                select u
                from User u
                left join fetch u.subscription s
                left join fetch s.permissions p
                where u.username = :username
            """)
    Optional<User> findByUsernameAndSubscription(@Param("username") String username);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.phoneNumber = :phoneNumber AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL")
    List<User> findAllActive();

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsernameAndNotDeleted(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.phoneNumber = :phoneNumber AND u.deletedAt IS NULL")
    boolean existsByPhoneNumberAndNotDeleted(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.userId = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Integer userId);

    Long countByIsActive(Boolean isActive);
}
