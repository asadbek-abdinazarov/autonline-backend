
// QuestionText.java
package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionTextId;

    @Column(columnDefinition = "TEXT")
    private String oz;
    @Column(columnDefinition = "TEXT")
    private String uz;
    @Column(columnDefinition = "TEXT")
    private String ru;

    @OneToOne
    @JoinColumn(name = "question_id", unique = true)
    private Question question;
}
