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

        LocalDateTime start = tr.getStartedAt();
        LocalDateTime finish = tr.getFinishedAt();

        String duration = null;

        if (start != null && finish != null) {
            Duration d = Duration.between(start, finish);
            duration = String.format(
                    "%02d:%02d:%02d",
                    d.toHours(),
                    d.toMinutesPart(),
                    d.toSecondsPart()
            );
        }

        return TestResultResponse.builder()
                .id(tr.getId())
                .score(tr.getScore())
                .duration(duration)
                .startedAt(start)
                .finishedAt(finish)
                .attemptNumber(tr.getAttemptNumber())
                .status(tr.getStatus() != null ? tr.getStatus().name() : null)
                .build();
    }

}
