package uz.javachi.autonline.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantResponseDTO {
    private Integer variantId;
    private Boolean isCorrect;
    private String text;
}
