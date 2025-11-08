package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerTextResponseDTO {
    private List<String> oz;
    private List<String> uz;
    private List<String> ru;
}
