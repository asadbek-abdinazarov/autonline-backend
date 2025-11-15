package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query(nativeQuery = true, value = "select count(*) from question where lesson_id = ?1")
    Long countQuestionByLesson(@Param("lessonId") Integer lessonId);

    @Query(value = "SELECT question_id FROM question ORDER BY RANDOM() LIMIT :interval", nativeQuery = true)
    List<Integer> findRandomQuestionIds(@Param("interval") Integer interval);
}
