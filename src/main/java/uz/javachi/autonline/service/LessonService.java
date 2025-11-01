package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.res.*;
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.projection.LessonAnonsProjection;
import uz.javachi.autonline.repository.LessonRepository;
import uz.javachi.autonline.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    private LessonResponseDTO getLessonResponseDTO(Lesson lesson) {
        return LessonResponseDTO.builder()
                .lessonId(lesson.getLessonId())
                .lessonName(lesson.getLessonName())
                .questions(lesson.getQuestions().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private QuestionResponseDTO convertToDTO(Question question) {
        return QuestionResponseDTO.builder()
                .questionId(question.getQuestionId())
                .photo(question.getPhoto())
                .questionText(convertQuestionText(question.getQuestionText()))
                .answers(convertAnswers(question.getAnswers(), question.getQuestionId()))
                .build();
    }

    private QuestionTextResponseDTO convertQuestionText(QuestionText questionText) {
        if (questionText == null) return null;

        return new QuestionTextResponseDTO(
                questionText.getOz(),
                questionText.getUz(),
                questionText.getRu()
        );
    }

    private AnswerResponseDTO convertAnswers(Answers answers, Integer questionId) {
        if (answers == null) return null;

        return new AnswerResponseDTO(
                answers.getAnswerId(),
                questionId,
                answers.getStatus(),
                convertAnswerText(answers.getAnswerText())
        );
    }

    private AnswerTextResponseDTO convertAnswerText(AnswerText answerText) {
        if (answerText == null) return null;

        return new AnswerTextResponseDTO(
                answerText.getOz(),
                answerText.getUz(),
                answerText.getRu()
        );
    }

    public ResponseEntity<LessonResponseDTO> getByLessonId(Integer lessonId) {
        Lesson lesson = lessonRepository.findLessonWithAllRelations(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));


        LessonResponseDTO lessonResponseDTO = new LessonResponseDTO();
        lessonResponseDTO.setLessonId(lesson.getLessonId());
        lessonResponseDTO.setLessonName(lesson.getLessonName());
        lessonResponseDTO.setLessonDescription(lesson.getLessonDescription());
        lessonResponseDTO.setLessonIcon(lesson.getLessonIcon());
        lessonResponseDTO.setLessonQuestionCount(questionRepository.countQuestionByLesson(lesson.getLessonId()));

        List<QuestionResponseDTO> questionResponses = lesson.getQuestions().stream().map(q -> {
            QuestionResponseDTO qr = new QuestionResponseDTO();
            qr.setQuestionId(q.getQuestionId());
            qr.setPhoto(q.getPhoto());

            QuestionText qt = q.getQuestionText();
            if (qt != null) {
                QuestionTextResponseDTO qtr = new QuestionTextResponseDTO();
                qtr.setOz(qt.getOz());
                qtr.setUz(qt.getUz());
                qtr.setRu(qt.getRu());
                qr.setQuestionText(qtr);
            }

            Answers answers = q.getAnswers();
            if (answers != null) {
                AnswerResponseDTO ar = new AnswerResponseDTO();
                ar.setAnswerId(answers.getAnswerId());
                ar.setStatus(answers.getStatus());
                ar.setQuestionId(q.getQuestionId());

                AnswerText at = answers.getAnswerText();
                if (at != null) {
                    AnswerTextResponseDTO atr = new AnswerTextResponseDTO();
                    atr.setOz(at.getOz());
                    atr.setUz(at.getUz());
                    atr.setRu(at.getRu());
                    ar.setAnswerText(atr);
                }

                qr.setAnswers(ar);
            }

            return qr;
        }).collect(Collectors.toList());

        lessonResponseDTO.setQuestions(questionResponses);

        return ResponseEntity.ok(lessonResponseDTO);
    }

    public List<LessonAnonsProjection> getAllLessonsAnons() {
        return lessonRepository.findAllLessonsAnons();
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordView(Integer lessonId, Optional<Integer> currentUserId) {
        lessonRepository.incrementViews(lessonId);
        if (currentUserId.isEmpty()) {
            lessonRepository.incrementUnique(lessonId);
        }
    }
}
