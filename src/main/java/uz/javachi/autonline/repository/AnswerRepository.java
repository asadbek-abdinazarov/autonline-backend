package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Answers;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.model.Question;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answers, Integer> {
    Optional<Answers> findAnswersByQuestion(Question question);
}
