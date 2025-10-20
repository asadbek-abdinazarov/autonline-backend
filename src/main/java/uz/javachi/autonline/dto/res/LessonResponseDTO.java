package uz.javachi.autonline.dto.res;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponseDTO {
    private Integer lessonId;
    private String lessonName;
    private String lessonDescription;
    private String lessonIcon;
    private Long lessonQuestionCount;
    private List<QuestionResponseDTO> questions;

    public LessonResponseDTO(Integer lessonId, String lessonName, String lessonDescription, String lessonIcon, Long lessonQuestionCount) {
        this.lessonId = lessonId;
        this.lessonName = lessonName;
        this.lessonDescription = lessonDescription;
        this.lessonIcon = lessonIcon;
        this.lessonQuestionCount = lessonQuestionCount;
    }
}
