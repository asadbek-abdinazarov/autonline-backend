package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.model.LessonHistory;
import uz.javachi.autonline.projection.LessonHistoryProjection;

import java.util.List;

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
                            LEFT JOIN users u ON u.user_id = lh.user_id
                            WHERE u.user_id = :current_user_id
                            ORDER BY lh.created_date DESC
            """)
    List<LessonHistoryProjection> getAllByUserId(@Param("current_user_id") Integer currentUserId, @Param("lang") String lang);

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
    Integer findByUserIdAndAvg(@Param("currentId") Integer currentId);


    @Query(nativeQuery = true, value = """
            SELECT avg(all_questions_count)
            FROM lesson_history
            WHERE user_id = :currentId
            """)
    Integer findByUserIdAndSuccessRate(@Param("currentId") Integer currentId);
}