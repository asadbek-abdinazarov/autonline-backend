package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.response.StatisticResponseDTO;
import uz.javachi.autonline.dto.response.UserLessonStatisticResponseDTO;
import uz.javachi.autonline.projection.LessonHistoryProjection;
import uz.javachi.autonline.repository.LessonHistoryRepository;
import uz.javachi.autonline.repository.PaymentHistoryRepository;
import uz.javachi.autonline.repository.QuestionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final UserRepository userRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final QuestionRepository questionRepository;
    private final LessonHistoryRepository lessonHistoryRepository;

    @Async("applicationTaskExecutor")
    public CompletableFuture<StatisticResponseDTO> getStatistic() {

        CompletableFuture<Long> activeUsersFuture = CompletableFuture.supplyAsync(() -> userRepository.countByIsActive(true));

        CompletableFuture<Long> paymentFuture = CompletableFuture.supplyAsync(() -> paymentHistoryRepository.countByIsPaid(true));

        CompletableFuture<Long> questionsFuture = CompletableFuture.supplyAsync(questionRepository::count);

        CompletableFuture<Long> lessonsFuture = CompletableFuture.supplyAsync(lessonHistoryRepository::count);

        return CompletableFuture.allOf(activeUsersFuture, paymentFuture, questionsFuture, lessonsFuture).thenApply(v -> {
            StatisticResponseDTO dto = new StatisticResponseDTO();
            try {
                dto.setAllActiveUserCount(activeUsersFuture.get());
                dto.setAllPaymentCount(paymentFuture.get());
                dto.setAllQuestionCount(questionsFuture.get());
                dto.setAllLessonHistoriesCount(lessonsFuture.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return dto;
        });
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<UserLessonStatisticResponseDTO> getUserLessonHistory() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        CompletableFuture<Long> totalTests = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.countByUserId(currentUserId));

        CompletableFuture<Long> passed = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.countByUserIdAndPercentageHigher(currentUserId));

        CompletableFuture<Integer> averageScore = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.findByUserIdAndAvg(currentUserId));

        CompletableFuture<Integer> successRate = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.findByUserIdAndSuccessRate(currentUserId));

        CompletableFuture<List<LessonHistoryProjection>> lessonHistories = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.getAllByUserId(currentUserId, lang));

        return CompletableFuture.allOf(totalTests, passed, averageScore, successRate, lessonHistories).thenApply(v -> {
            UserLessonStatisticResponseDTO dto = new UserLessonStatisticResponseDTO();

            try {
                dto.setTotalTests(totalTests.get());
                dto.setPassed(passed.get());
                dto.setAverageScore(averageScore.get());
                dto.setSuccessRate(successRate.get());
                dto.setLessonHistories(lessonHistories.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return dto;
        });

    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<UserLessonStatisticResponseDTO> getUserLessonHistory(Integer userId) {
        String lang = LocaleContextHolder.getLocale().getLanguage();

        CompletableFuture<Long> totalTests = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.countByUserId(userId));

        CompletableFuture<Long> passed = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.countByUserIdAndPercentageHigher(userId));

        CompletableFuture<Integer> averageScore = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.findByUserIdAndAvg(userId));

        CompletableFuture<Integer> successRate = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.findByUserIdAndSuccessRate(userId));

        CompletableFuture<List<LessonHistoryProjection>> lessonHistories = CompletableFuture.supplyAsync(() -> lessonHistoryRepository.getAllByUserId(userId, lang));

        return CompletableFuture.allOf(totalTests, passed, averageScore, successRate, lessonHistories).thenApply(v -> {
            UserLessonStatisticResponseDTO dto = new UserLessonStatisticResponseDTO();

            try {
                dto.setTotalTests(totalTests.get());
                dto.setPassed(passed.get());
                dto.setAverageScore(averageScore.get());
                dto.setSuccessRate(successRate.get());
                dto.setLessonHistories(lessonHistories.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return dto;
        });
    }
}
