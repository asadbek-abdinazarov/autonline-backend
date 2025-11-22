package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.projection.LessonAnonsProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    @Query("select l from Lesson l")
    List<Lesson> findAllWithAllTranslationsAndRelations();

    @Query(nativeQuery = true, value = """
            select * from lesson where lesson_id = :lessonId
            """)
    Optional<Lesson> findByLessonId(@Param("lessonId") Integer lessonId);

    @SuppressWarnings("unused")
    @Query(nativeQuery = true,
            value = """
                        select l from Lesson l
                        left join lesson_translation lt on l.lesson_id = lt.lesson_id
                        left join question q on q.lesson_id = l.lesson_id
                        left join question_translation qt on qt.question_id = q.question_id
                        left join variants v on v.question_id = q.question_id
                        left join variant_translation vt on vt.variant_id = v.variant_id
                        where l.lesson_id = :id
                    """)
    Optional<Lesson> getLessonFull(@Param("id") Integer id);

    @Query(nativeQuery = true, value = """
                SELECT
                    l.lesson_id AS id,
                    lt.name AS name,
                    lt.description AS description,
                    l.lesson_icon AS icon,
                    COUNT(q.question_id) AS lessonQuestionCount,
                    l.views_count AS view_count
                FROM lesson l
                LEFT JOIN question q
                    ON l.lesson_id = q.lesson_id
                LEFT JOIN lesson_translation lt
                    ON l.lesson_id = lt.lesson_id
                   AND lt.lang = :lang
                GROUP BY
                    l.lesson_id, lt.name, lt.description, l.lesson_icon, l.views_count
            """)
    List<LessonAnonsProjection> findAllWithTranslations(@Param("lang") String lang);

    @Query(
            value = "SELECT lesson_id FROM lesson ORDER BY RANDOM() LIMIT :interval",
            nativeQuery = true
    )
    List<Integer> findRandomLessons(@Param("interval") Integer interval);

    @Query(value = """
            SELECT DISTINCT l.*
            FROM lesson l
            JOIN question q ON q.lesson_id = l.lesson_id
            WHERE l.lesson_id IN (:lessonIds)
              AND q.question_id IN (:questionIds)
            limit :interval
            """, nativeQuery = true)
    List<Lesson> loadLessonsWithQuestionsAndVariants(
            @Param("lessonIds") List<Integer> lessonIds,
            @Param("questionIds") List<Integer> questionIds,
            @Param("lang") String lang,
            @Param("interval") Integer interval);

    @Query(nativeQuery = true, value = """
            select * from lesson where lesson_id = :randomLessonId
            """)
    Lesson findRandomLesson(@Param("randomLessonId") Integer randomLessonId);
}
