package uz.javachi.autonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.model.LessonHistory;
import uz.javachi.autonline.projection.LessonHistoryProjection;

public interface LessonHistoryRepository extends JpaRepository<LessonHistory, Long> {
    @Query(nativeQuery = true, value = """
                    SELECT
                                lh.lesson_history_id as lessonHistoryId,
                                lh.percentage as percentage,
                                lh.correct_answers_count as correctAnswersCount,
                                lh.not_correct_answers_count as notCorrectAnswersCount,
                                lh.all_questions_count as allQuestionCount,
                                lh.created_date as createdDate,
                                lt.name as lessonName,
                                l.lesson_icon as lessonIcon
                    FROM lesson_history lh
                                LEFT JOIN lesson l ON l.lesson_id = lh.lesson_id
                                LEFT JOIN lesson_translation lt
                                    ON lt.lesson_id = l.lesson_id
                                    AND LOWER(lt.lang) = LOWER(:lang)
                    WHERE lh.user_id = :currentId
                    ORDER BY lh.created_date DESC
            """,
            countQuery = """
                            SELECT count(*) FROM lesson_history lh WHERE lh.user_id = :currentId
                    """)
    Page<LessonHistoryProjection> getAllByUserId(
            @Param("lang") String lang,
            @Param("currentId") Integer currentId,
            Pageable pageable
    );

    Long countByUserId(Integer userId);

    @Query(nativeQuery = true, value = """
            SELECT count(*)
            FROM lesson_history lh
            WHERE lh.user_id = :currentId
              AND lh.percentage >= 70
            """)
    Long countByUserIdAndPercentageHigher(@Param("currentId") Integer currentId);


    @Query(nativeQuery = true, value = """
            SELECT avg(correct_answers_count)
            FROM lesson_history
            WHERE user_id = :currentId
            """)
    Double findByUserIdAndAvg(@Param("currentId") Integer currentId);


    @Query(nativeQuery = true, value = """
                SELECT
                    CASE
                        WHEN SUM(all_questions_count) = 0 THEN 0
                        ELSE (SUM(correct_answers_count)::float / SUM(all_questions_count)::float) * 100
                    END
                FROM lesson_history
                WHERE user_id = :currentId
            """)
    Double findSuccessRate(@Param("currentId") Integer currentId);
}