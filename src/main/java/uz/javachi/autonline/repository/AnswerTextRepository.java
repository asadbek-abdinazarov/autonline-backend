package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.AnswerText;
import uz.javachi.autonline.model.Answers;
import uz.javachi.autonline.model.Question;
import uz.javachi.autonline.model.QuestionText;

import java.util.Optional;

@Repository
public interface AnswerTextRepository extends JpaRepository<AnswerText, Integer> {
    Optional<AnswerText> findAnswerTextByAnswer(Answers answer);

}
