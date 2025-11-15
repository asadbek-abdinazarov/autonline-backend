package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
    private Integer questionId;
    private String photo;
    private String questionText;
    private List<VariantResponseDTO> variants;
}
