package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final UserRepository userRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final QuestionRepository questionRepository;
    private final LessonHistoryRepository lessonHistoryRepository;
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#0.0");


    @Async("applicationTaskExecutor")
    public CompletableFuture<StatisticResponseDTO> getStatistic() {

        CompletableFuture<Long> activeUsersFuture = supply(() -> userRepository.countByIsActive(true));

        CompletableFuture<Long> paymentFuture = supply(() -> paymentHistoryRepository.countByIsPaid(true));

        CompletableFuture<Long> questionsFuture = supply(questionRepository::count);

        CompletableFuture<Long> lessonsFuture = supply(lessonHistoryRepository::count);

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
    public CompletableFuture<UserLessonStatisticResponseDTO> getUserLessonHistory(int page, int size) {
        Integer userId = SecurityUtils.getCurrentUserIdOrThrow();
        Pageable pageable = PageRequest.of(page, size);

        return buildUserLessonStatistics(userId, pageable);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<UserLessonStatisticResponseDTO> getUserLessonHistory(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return buildUserLessonStatistics(userId, pageable);
    }

    private CompletableFuture<UserLessonStatisticResponseDTO> buildUserLessonStatistics(
            Integer userId,
            Pageable pageable
    ) {
        String lang = LocaleContextHolder.getLocale().getLanguage();

        CompletableFuture<Long> totalTests = supply(() ->
                lessonHistoryRepository.countByUserId(userId));

        CompletableFuture<Long> passed = supply(() ->
                lessonHistoryRepository.countByUserIdAndPercentageHigher(userId));

        CompletableFuture<Double> avgScore = supply(() ->
                lessonHistoryRepository.findByUserIdAndAvg(userId));

        CompletableFuture<Double> successRate = supply(() ->
                lessonHistoryRepository.findSuccessRate(userId));

        CompletableFuture<Page<LessonHistoryProjection>> histories = supply(() ->
                lessonHistoryRepository.getAllByUserId(lang, userId, pageable));

        return CompletableFuture.allOf(totalTests, passed, avgScore, successRate, histories)
                .thenApply(v -> {
                    DecimalFormat df = new DecimalFormat("#0.0");

                    Double join = avgScore.join();
                    if (join == null) {
                        join = 0.0;
                    }
                    Double join1 = successRate.join();
                    if (join1 == null) {
                        join1 = 0.0;
                    }
                    return UserLessonStatisticResponseDTO.builder()
                            .totalTests(totalTests.join())
                            .passed(passed.join())
                            .averageScore(df.format(join))
                            .successRate(df.format(join1))
                            .lessonHistories(histories.join())
                            .build();
                });
    }

    /**
     * Short alias for supplyAsync using same executor
     */
    private <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }
}
