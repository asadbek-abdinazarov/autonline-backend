package uz.javachi.autonline.dto.response;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonHistoryDTO implements Serializable {
    private Integer lessonId;
    private Integer percentage;
    private Integer allQuestionsCount;
    private Integer correctAnswersCount;
    private Integer notCorrectAnswersCount;
}
