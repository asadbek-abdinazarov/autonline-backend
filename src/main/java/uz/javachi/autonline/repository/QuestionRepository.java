package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.model.Question;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Optional<List<Question>> findQuestionByLesson(Lesson lesson);

    @Query(nativeQuery = true, value = "select count(*) from question where lesson_id = ?1")
    Long countQuestionByLesson(@Param("lessonId") Integer lessonId);
}
