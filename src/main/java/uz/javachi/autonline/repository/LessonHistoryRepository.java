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
                            lh.lesson_history_id,
                                    lh.percentage,
                                                lh.correct_answers_count,
                                                            lh.not_correct_answers_count,
                                                                        lh.all_questions_count,
                                                                                    lh.created_date,
                                                                                                l.lesson_name,
                                                                                                            l.lesson_icon
            from lesson_history lh
            left join lesson l on l.lesson_id = lh.lesson_id
            left join users u on u.user_id = lh.user_id where u.user_id = :current_user_id
            order by lh.created_date desc
            """)
    List<LessonHistoryProjection> getAllByUserId(@Param("current_user_id") Integer currentUserId);
}