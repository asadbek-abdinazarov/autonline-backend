package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lesson_history"/*,
        indexes = {
                @Index(name = "idx_payment_user_id", columnList = "user_id"),
                @Index(name = "idx_payment_date", columnList = "payment_date"),
                @Index(name = "idx_payment_status", columnList = "is_paid")
        }*/)
public class LessonHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonHistoryId;

    private Integer userId;

    private Integer lessonId;

    @Column(columnDefinition = "smallint")
    private Integer percentage;

    @Column(columnDefinition = "smallint")
    private Integer allQuestionsCount;

    @Column(columnDefinition = "smallint")
    private Integer correctAnswersCount;

    @Column(columnDefinition = "smallint")
    private Integer notCorrectAnswersCount;

    @CreationTimestamp
    private LocalDateTime createdDate;
}
