package uz.javachi.autonline.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestTemplateResponseDTO {

    private Integer id;
    private String title;
    private Integer duration;
    private Integer maxScore;
    private Integer passScore;
    private LocalDateTime createdAt;

    private TestResultResponse testResultResponse;

}
