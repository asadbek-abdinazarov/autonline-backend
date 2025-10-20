
// Question.java
package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    private String question;

    private String photo;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private QuestionText questionText;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answers answers;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
}