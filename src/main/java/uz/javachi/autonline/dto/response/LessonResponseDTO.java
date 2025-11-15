package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponseDTO {
    private Integer id;
    private String icon;
    private Long viewsCount;
    private String name;
    private String description;
    private List<QuestionResponseDTO> questions;
}
