package uz.javachi.autonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query(nativeQuery = true, value = """
                    SELECT DISTINCT q.*
            FROM question q
                     LEFT JOIN question_translation qt ON q.question_id = qt.question_id
                     LEFT JOIN variants v ON v.question_id = q.question_id
                     LEFT JOIN variant_translation vt ON v.variant_id = vt.variant_id
            ORDER BY q.question_id
            """)
    Page<Question> findByInterval(
            Pageable pageable
    );

    @Query(value = """
                SELECT q.question_id,q.photo , q.lesson_id, qt.question_text
                FROM question q
                LEFT JOIN question_translation qt on q.question_id = qt.question_id
                    WHERE qt.lang = :lang
                ORDER BY RANDOM()
                LIMIT :interval
            """, nativeQuery = true)
    List<Question> findRandomQuestions(@Param("interval") Integer interval, @Param("lang") String lang);

    @Query(nativeQuery = true, value = """
            select * from question q where q.photo is not null and length(q.photo) < 20 limit 350
            """)
    List<Question> findQuestionByPhotoIsNotNull();

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.photo = :photo WHERE q.questionId = :questionId")
    int updatePhoto(@Param("photo") String photo, @Param("questionId") Integer questionId);
}
