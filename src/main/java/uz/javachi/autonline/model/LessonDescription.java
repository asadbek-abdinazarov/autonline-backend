package uz.javachi.autonline.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class LessonDescription {
    private String descriptionUz;
    private String descriptionOz;
    private String descriptionRu;
}
