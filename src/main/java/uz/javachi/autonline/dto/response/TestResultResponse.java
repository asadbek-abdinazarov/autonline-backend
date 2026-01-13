package uz.javachi.autonline.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResultResponse {
    private Long id;
    private Integer score;
    private String status;
    private Integer attemptNumber;
    private String duration;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}