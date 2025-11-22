package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query(value = """
                SELECT q.question_id,q.photo , q.lesson_id, qt.question_text
                FROM question q
                LEFT JOIN question_translation qt on q.question_id = qt.question_id
                    WHERE qt.lang = :lang
                ORDER BY RANDOM()
                LIMIT :interval
            """, nativeQuery = true)
    List<Question> findRandomQuestions(@Param("interval") Integer interval, @Param("lang") String lang);
}
