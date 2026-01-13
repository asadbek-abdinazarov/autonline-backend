package uz.javachi.autonline.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinishResponseDTO {
    private Long testResultId;
    private Integer score;
    private Integer percentage;
    private Integer correctCount;
    private Integer wrongCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
