package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.projection.LessonAnonsProjection;
import uz.javachi.autonline.model.Lesson;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    @Query("select l from Lesson l")
    List<Lesson> findAllWithAllTranslationsAndRelations();

    Optional<Lesson> findByLessonId(Integer lessonId);

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
}
