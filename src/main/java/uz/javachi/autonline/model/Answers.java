
package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerId;

    private int status;

    @OneToOne(mappedBy = "answer", cascade = CascadeType.ALL)
    private AnswerText answerText;

    @OneToOne
    @JoinColumn(name = "question_id", unique = true)
    private Question question;
}
