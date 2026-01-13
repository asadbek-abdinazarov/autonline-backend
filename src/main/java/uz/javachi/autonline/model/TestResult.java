package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import uz.javachi.autonline.dto.response.TestResultResponse;
import uz.javachi.autonline.enums.TestStatus;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "test_template_id")
    private TestTemplate testTemplate;

    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    private TestStatus status;

    private Integer attemptNumber;

    public static TestResultResponse toResponseDto(TestResult tr) {

        Duration duration = Duration.between(
                tr.getStartedAt(),
                tr.getFinishedAt()
        );

        return TestResultResponse.builder()
                .id(tr.getId())
                .score(tr.getScore())
                .duration(
                        String.format("%02d:%02d:%02d",
                                duration.toHours(),
                                duration.toMinutesPart(),
                                duration.toSecondsPart())
                )
                .startedAt(tr.getStartedAt())
                .finishedAt(tr.getFinishedAt())
                .attemptNumber(tr.getAttemptNumber())
                .status(tr.getStatus().name())
                .build();
    }

}
