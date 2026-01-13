package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartedResponseDTO {
    private Long testResultId;
    private Integer testTemplateId;
    private Integer lessonId;
    private String icon;
    private String name;
    private String description;
    private List<QuestionResponseDTO> questions;
}
