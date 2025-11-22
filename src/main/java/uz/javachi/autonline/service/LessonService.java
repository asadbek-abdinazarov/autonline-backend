package uz.javachi.autonline.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import uz.javachi.autonline.config.Localized;
import uz.javachi.autonline.dto.response.LessonResponseDTO;
import uz.javachi.autonline.dto.response.QuestionResponseDTO;
import uz.javachi.autonline.dto.response.VariantResponseDTO;
import uz.javachi.autonline.exceptions.IntervalInvalidException;
import uz.javachi.autonline.model.*;
import uz.javachi.autonline.projection.LessonAnonsProjection;
import uz.javachi.autonline.repository.LessonRepository;
import uz.javachi.autonline.repository.QuestionRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uz.javachi.autonline.DefaultValues.RANDOM_LESSON_ID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final MessageService messageService;


    @SuppressWarnings("unused")
    public List<LessonResponseDTO> getAllLessons() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        List<Lesson> lessons = lessonRepository.findAllWithAllTranslationsAndRelations();
        return lessons.stream()
                .map(l -> mapLessonToDto(l, lang))
                .toList();
    }

    public LessonResponseDTO getLesson(Integer id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: %s".formatted(id)));
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return mapLessonToDto(lesson, lang);
    }

    private LessonResponseDTO mapLessonToDto(Lesson lesson, String lang) {
        LessonTranslation t = findTranslation(lesson.getTranslations(), lang);
        LessonResponseDTO dto = new LessonResponseDTO();
        dto.setId(lesson.getLessonId());
        dto.setIcon(lesson.getLessonIcon());
        dto.setViewsCount(lesson.getViewsCount());
        dto.setName(t != null ? t.getName() : null);
        dto.setDescription(t != null ? t.getDescription() : null);

        List<QuestionResponseDTO> qs = getQuestionResponseDTOS(lesson.getQuestions(), lang);

        dto.setQuestions(qs);
        return dto;
    }

    private List<QuestionResponseDTO> getQuestionResponseDTOS(List<Question> question, String lang) {
        return question.stream()
                .map(q -> {
                    QuestionResponseDTO qdto = new QuestionResponseDTO();
                    qdto.setQuestionId(q.getQuestionId());
                    qdto.setPhoto(q.getPhoto());
                    QuestionTranslation qt = findTranslation(q.getTranslations(), lang);
                    qdto.setQuestionText(qt != null ? qt.getQuestionText() : null);

                    List<VariantResponseDTO> vs = q.getVariants().stream()
                            .map(v -> {
                                VariantResponseDTO vd = new VariantResponseDTO();
                                vd.setVariantId(v.getVariantId());
                                vd.setIsCorrect(v.getIsCorrect());
                                VariantTranslation vt = findTranslation(v.getTranslations(), lang);
                                vd.setText(vt != null ? vt.getText() : null);
                                return vd;
                            }).collect(Collectors.toList());
                    qdto.setVariants(vs);
                    return qdto;
                }).collect(Collectors.toList());
    }

    private <X extends Localized> X findTranslation(Collection<X> translations, String lang) {
        if (translations == null || translations.isEmpty()) return null;

        return translations.stream()
                .filter(t -> lang.equalsIgnoreCase(t.getLang()))
                .findFirst()
                .orElse(null);
    }

    public List<LessonAnonsProjection> getLessonsAnons() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        List<LessonAnonsProjection> lessons = lessonRepository.findAllWithTranslations(lang);
        if (lessons.isEmpty()) return null;
        return lessons;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<LessonResponseDTO> getRandomQuiz(Integer interval) {
        try {
            if (interval == null) return ResponseEntity.ok(new LessonResponseDTO());

            if (interval < 5 || interval > 100)
                throw new IntervalInvalidException(messageService.get("lesson.invalid.interval"));

            String lang = LocaleContextHolder.getLocale().getLanguage();
            Lesson randomLesson = lessonRepository.findRandomLesson(RANDOM_LESSON_ID);
            List<Question> questions = questionRepository.findRandomQuestions(interval, lang);

            LessonResponseDTO result = new LessonResponseDTO();
            result.setId(randomLesson.getLessonId());
            result.setIcon(randomLesson.getLessonIcon());
            LessonTranslation t = findTranslation(randomLesson.getTranslations(), lang);
            result.setName(t != null ? t.getName() : null);
            result.setDescription(t != null ? t.getDescription() : null);
            result.setViewsCount(randomLesson.getViewsCount());
            List<QuestionResponseDTO> qs = getQuestionResponseDTOS(questions, lang);
            result.setQuestions(qs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in getRandomQuiz: ", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void recordView(Integer topicId, Optional<Integer> currentUserId) {

        if (currentUserId.isEmpty()) {
            throw new EntityNotFoundException("Current user id is empty");
        }

        Lesson lesson = lessonRepository.findByLessonId(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: %s".formatted(topicId)));

        Long views = Optional.ofNullable(lesson.getViewsCount()).orElse(0L);
        lesson.setViewsCount(views + 1);

        lessonRepository.saveAndFlush(lesson);

        log.info("Recording view for lesson {}, viewer id: {}", topicId, currentUserId.get());
    }
}
