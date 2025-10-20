
// AnswerText.java
package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerTextId;

     @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "answer_text_oz", joinColumns = @JoinColumn(name = "answer_text_id"))
    @Column(name = "oz_value", columnDefinition = "TEXT")
    private List<String> oz;

     @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "answer_text_uz", joinColumns = @JoinColumn(name = "answer_text_id"))
    @Column(name = "uz_value", columnDefinition = "TEXT")
    private List<String> uz;

     @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "answer_text_ru", joinColumns = @JoinColumn(name = "answer_text_id"))
    @Column(name = "ru_value", columnDefinition = "TEXT")
    private List<String> ru;

    @OneToOne
    @JoinColumn(name = "answer_id", unique = true)
    private Answers answer;
}