package uz.javachi.autonline.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.config.Localized;
import uz.javachi.autonline.projection.LessonAnonsProjection;
import uz.javachi.autonline.dto.response.LessonResponseDTO;
import uz.javachi.autonline.dto.response.QuestionResponseDTO;
import uz.javachi.autonline.dto.response.VariantResponseDTO;
import uz.javachi.autonline.model.Lesson;
import uz.javachi.autonline.model.LessonTranslation;
import uz.javachi.autonline.model.QuestionTranslation;
import uz.javachi.autonline.model.VariantTranslation;
import uz.javachi.autonline.repository.LessonRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;


    public List<LessonResponseDTO> getAllLessons() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        List<Lesson> lessons = lessonRepository.findAllWithAllTranslationsAndRelations();
        return lessons.stream()
                .map(l -> mapLessonToDto(l, lang))
                .toList();
    }

    public LessonResponseDTO getLesson(Integer id) {
        Lesson lesson = lessonRepository.findByLessonId(id)
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

        List<QuestionResponseDTO> qs = lesson.getQuestions().stream()
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

        dto.setQuestions(qs);
        return dto;
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

    public ResponseEntity<List<LessonResponseDTO>> getRandomQuiz(Integer interval) {
        return null;
    }
}
