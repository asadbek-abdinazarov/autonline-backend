package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import uz.javachi.autonline.config.Localized;

@Entity
@Table(name = "lesson_translation"/*, indexes = {
        @Index(name = "idx_lesson_lang", columnList = "lesson_id, lang")
}*/)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonTranslation implements Localized {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 10, nullable = false)
    private String lang;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    public LessonTranslation(String lang, String name) {
        this.lang = lang;
        this.name = name;
    }

    @Override
    public String toString() {
        return "LessonTranslation{" +
                "id=" + id +
                ", lang='" + lang + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

