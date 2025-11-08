package uz.javachi.autonline.dto.res;

import lombok.Data;

@Data
public class StatisticResponseDTO {
    private Long allQuestionCount;
    private Long allLessonHistoriesCount;
    private Long allActiveUserCount;
    private Long allPaymentCount;
}
