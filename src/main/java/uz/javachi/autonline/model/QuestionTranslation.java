package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import uz.javachi.autonline.config.Localized;

@Entity
@Table(name = "question_translation"/*, indexes = {
        @Index(name = "idx_question_lang", columnList = "question_id, lang")
}*/)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTranslation implements Localized {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 10, nullable = false)
    private String lang;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Override
    public String toString() {
        return "QuestionTranslation{" +
                "id=" + id +
                ", lang='" + lang + '\'' +
                ", questionText='" + questionText + '\'' +
                '}';
    }
}
