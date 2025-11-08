package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.res.StatisticResponseDTO;
import uz.javachi.autonline.repository.LessonHistoryRepository;
import uz.javachi.autonline.repository.PaymentHistoryRepository;
import uz.javachi.autonline.repository.QuestionRepository;
import uz.javachi.autonline.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final UserRepository userRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final QuestionRepository questionRepository;
    private final LessonHistoryRepository lessonHistoryRepository;

    public Optional<StatisticResponseDTO> getStatistic() {

        StatisticResponseDTO statistic = new StatisticResponseDTO();
        statistic.setAllActiveUserCount(userRepository.countByIsActive(true));
        statistic.setAllPaymentCount(paymentHistoryRepository.countByIsPaid(true));
        statistic.setAllQuestionCount(questionRepository.count());
        statistic.setAllLessonHistoriesCount(lessonHistoryRepository.count());

        return Optional.of(statistic);
    }
}
