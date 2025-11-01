package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.projection.LessonAnonsProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    @Query("SELECT l FROM Lesson l " +
            "LEFT JOIN FETCH l.questions q " +
            "LEFT JOIN FETCH q.questionText qt " +
            "LEFT JOIN FETCH q.answers a " +
            "LEFT JOIN FETCH a.answerText at")
    List<Lesson> findAllWithQuestions();

    @Query("SELECT l FROM Lesson l " +
            "LEFT JOIN FETCH l.questions q " +
            "LEFT JOIN FETCH q.questionText qt " +
            "LEFT JOIN FETCH q.answers a " +
            "LEFT JOIN FETCH a.answerText at " +
            "WHERE l.lessonId = :lessonId ORDER BY l.lessonId")
    Optional<Lesson> findLessonWithAllRelations(@Param("lessonId") Integer lesson);


    @Query("SELECT new uz.javachi.autonline.dto.res.LessonResponseDTO(" +
            "l.lessonId, l.lessonName, l.lessonDescription, l.lessonIcon, COUNT(q), l.viewsCount)" +
            "FROM Lesson l " +
            "LEFT JOIN l.questions q " +
            "GROUP BY l.lessonId, l.lessonName, l.lessonDescription, l.lessonIcon order by count(q)")
    List<LessonAnonsProjection> findAllLessonsAnons();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "UPDATE lesson SET views_count = views_count + 1 WHERE lesson_id = :lessonId")
    void incrementViews(@Param("lessonId") Integer lessonId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE lesson SET unique_count = unique_count + 1 WHERE lesson_id = :topicId")
    void incrementUnique(@Param("lessonId") Integer lessonId);
}
