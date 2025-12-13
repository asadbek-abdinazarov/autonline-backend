package uz.javachi.autonline.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;
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
    private String averageScore;
    private String successRate;
    private Page<LessonHistoryProjection> lessonHistories;
}
