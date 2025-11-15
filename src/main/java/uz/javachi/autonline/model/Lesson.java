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
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;
    private String lessonIcon;

    @Enumerated
    private LessonName lessonNames;
    @Enumerated
    private LessonDescription lessonDescription;

    @Column(name = "views_count")
    private Long viewsCount = 0L;

    @Column(name = "unique_count")
    private Long uniqueCount = 0L;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<Question> questions;
}