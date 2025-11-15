package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.response.LessonHistoryDTO;
import uz.javachi.autonline.model.LessonHistory;
import uz.javachi.autonline.projection.LessonHistoryProjection;
import uz.javachi.autonline.repository.LessonHistoryRepository;

import java.util.List;

import static uz.javachi.autonline.utils.SecurityUtils.getCurrentUserIdOrThrow;

@Service
@RequiredArgsConstructor
public class LessonHistoryService {

    private final LessonHistoryRepository lessonHistoryRepository;

    public ResponseEntity<List<LessonHistoryProjection>> getAllMyLessonHistory() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        Integer currentUserId = getCurrentUserIdOrThrow();
        return ResponseEntity.ok(lessonHistoryRepository.getAllByUserId(currentUserId, lang));

    }

    public ResponseEntity<?> createLessonHistory(LessonHistoryDTO lessonHistoryDTO) {
        LessonHistory build = LessonHistory.builder()
                .lessonId(lessonHistoryDTO.getLessonId())
                .allQuestionsCount(lessonHistoryDTO.getAllQuestionsCount())
                .correctAnswersCount(lessonHistoryDTO.getCorrectAnswersCount())
                .notCorrectAnswersCount(lessonHistoryDTO.getNotCorrectAnswersCount())
                .percentage(lessonHistoryDTO.getPercentage())
                .userId(getCurrentUserIdOrThrow())
                .build();
        lessonHistoryRepository.save(build);
        return ResponseEntity.ok("Successfully created lesson history!");
    }
}
