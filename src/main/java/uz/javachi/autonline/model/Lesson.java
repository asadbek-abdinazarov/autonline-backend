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
    private String lessonName;
    private String lessonDescription;
    private String lessonIcon;
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<Question> questions;
}