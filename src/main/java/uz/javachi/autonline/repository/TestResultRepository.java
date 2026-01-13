package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.javachi.autonline.model.TestResult;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    // user testni necha marta yechgan
//    int countByUserIdAndTestTemplateId(Long userId, Long testTemplateId);

    // userning bitta test bo‘yicha barcha urinishlari
   /* List<TestResult> findByUserIdAndTestTemplateIdOrderByAttemptNumberDesc(
            Long userId, Long testTemplateId
    );*/

    // user yechgan barcha testlar
//    List<TestResult> findByUserId(Long userId);

    // oxirgi urinish
   /* Optional<TestResult> findTopByUserIdAndTestTemplateIdOrderByAttemptNumberDesc(
            Long userId, Long testTemplateId
    );
    @Query("""
SELECT COUNT(tr)
FROM TestResult tr
WHERE tr.user.id = :userId AND tr.status = 'PASSED'
""")
long countPassedTests(Long userId);

@Query("""
SELECT tr.testTemplate
FROM TestResult tr
WHERE tr.user.id = :userId AND tr.status = 'FAILED'
""")
List<TestTemplate> failedTests(Long userId);

    */
}