package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.model.TestResult;

import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    @Query(nativeQuery = true, value = """
            SELECT * FROM test_results tr where tr.test_template_id = :testTemplateId and tr.finished_at is null
            """)
    Optional<TestResult> findLastAndNotFinishedAndByTemplateId(@Param("testTemplateId") Integer testTemplateId);
}