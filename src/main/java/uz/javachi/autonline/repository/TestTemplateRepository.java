package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.model.TestTemplate;

import java.util.List;

public interface TestTemplateRepository extends JpaRepository<TestTemplate, Integer> {

    @Query("""
            SELECT tt, tr
            FROM TestTemplate tt
            LEFT JOIN TestResult tr
               ON tr.testTemplate = tt
              AND tr.user.userId = :userId
              AND tr.id = (
                  SELECT MAX(tr2.id)
                  FROM TestResult tr2
                  WHERE tr2.testTemplate = tt
                    AND tr2.user.userId = :userId AND tr.startedAt = (
                                                      SELECT MAX(tr2.startedAt)
                                                      FROM TestResult tr2
                                                      WHERE tr2.testTemplate = tt
                                                        AND tr2.user.userId = :userId
                                                  )
              )
            ORDER BY tt.testTemplateId
            """)
    List<Object[]> findAllTemplatesWithLastResult(@Param("userId") Integer userId);


}