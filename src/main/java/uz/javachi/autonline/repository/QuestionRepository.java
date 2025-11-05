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

    @Query(value = "SELECT question_id FROM question ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Integer> findRandomQuestionIds();

    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.questionText qt " +
            "LEFT JOIN FETCH q.answers a " +
            "LEFT JOIN FETCH a.answerText at " +
            "LEFT JOIN FETCH q.lesson l " +
            "WHERE q.questionId IN :questionIds")
    List<Question> findQuestionsByIdsWithAllRelations(@Param("questionIds") List<Integer> questionIds);
}
