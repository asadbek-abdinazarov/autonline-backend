package uz.javachi.autonline.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class LessonName {
    private String nameUz;
    private String nameOz;
    private String nameRu;
}
