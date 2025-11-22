package uz.javachi.autonline.dto.response;

import lombok.*;
import uz.javachi.autonline.projection.LessonHistoryProjection;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonStatisticResponseDTO {
    private Long totalTests;
    private Long passed;
    private Integer averageScore;
    private Integer successRate;
    private List<LessonHistoryProjection> lessonHistories;
}
