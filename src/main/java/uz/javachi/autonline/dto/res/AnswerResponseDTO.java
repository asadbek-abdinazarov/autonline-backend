package uz.javachi.autonline.dto.res;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponseDTO {
    private Integer answerId;
    private Integer questionId;
    private int status;
    private AnswerTextResponseDTO answerText;
}
