package uz.javachi.autonline.dto.res;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDTO {

    private Integer questionId;
    private String photo;
    private QuestionTextResponseDTO questionText;
    private AnswerResponseDTO answers;
}
