package uz.javachi.autonline.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("""
                SELECT u FROM User u
                LEFT JOIN FETCH u.students
                WHERE u.userId = :id
            """)
    Optional<User> findTeacherWithStudents(@Param("id") Integer id);


    @Query(value = """
                select u
                from User u
                left join fetch u.subscription s
                left join fetch s.permissions p
                where u.username = :username
                            and u.deletedAt is null
                                        and u.isActive
                                                    and s.isActive = true
                                                                and s.deletedAt is null
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

    @Query("SELECT u FROM User u WHERE u.userId <> :userId")
    Page<User> findAllWithoutHimSelf(@Param("userId") Integer userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
            UPDATE user_subscriptions
            SET subscription_id = :subscriptionId
            WHERE user_id = :userId
            """)
    int updateUserSubscription(@Param("userId") Integer userId,
                               @Param("subscriptionId") Integer subscriptionId);

    @Query(value = """
                      SELECT u.*, s.subscription_id FROM users u
                      JOIN teacher_students ts ON ts.student_id = u.user_id
                      JOIN user_subscriptions us on us.user_id = u.user_id
                      LEFT JOIN subscription s on s.subscription_id = us.subscription_id
                      WHERE ts.teacher_id = :teacherId
                        AND ts.student_id = :studentId
                        AND s.is_active = true
                        AND s.deleted_at IS NULL
            """, nativeQuery = true)
    Optional<User> findStudentOfTeacher(Integer teacherId, Integer studentId);
}
