package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.model.Question;
import uz.javachi.autonline.model.QuestionText;

import java.util.Optional;

@Repository
public interface QuestionTextRepository extends JpaRepository<QuestionText, Integer> {
    Optional<QuestionText> findQuestionTextByQuestion(Question question);
}
