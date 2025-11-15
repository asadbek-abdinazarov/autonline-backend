package uz.javachi.autonline.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class LessonResponseDTO {
    private Integer lessonId;
    private String nameUz;
    private String nameOz;
    private String nameRu;
    private String descriptionUz;
    private String descriptionOz;
    private String descriptionRu;
    private String lessonIcon;
    private Long lessonQuestionCount;
    private Long lessonViewsCount;
    private List<QuestionResponseDTO> questions;

    public LessonResponseDTO(Integer lessonId,
                             String nameUz,
                             String nameOz,
                             String nameRu,
                             String descriptionUz,
                             String descriptionOz,
                             String descriptionRu,
                             String lessonIcon,
                             Long lessonQuestionCount,
                             Long lessonViewsCount) {
        this.lessonId = lessonId;
        this.nameUz = nameUz;
        this.nameOz = nameOz;
        this.nameRu = nameRu;
        this.descriptionUz = descriptionUz;
        this.descriptionOz = descriptionOz;
        this.descriptionRu = descriptionRu;
        this.lessonIcon = lessonIcon;
        this.lessonQuestionCount = lessonQuestionCount;
        this.lessonViewsCount = lessonViewsCount;
    }

    public LessonResponseDTO(Integer lessonId, String nameUz, String nameOz, String nameRu, String descriptionUz, String descriptionOz, String descriptionRu, String lessonIcon, Long lessonQuestionCount, Long lessonViewsCount, List<QuestionResponseDTO> questions) {
        this.lessonId = lessonId;
        this.nameUz = nameUz;
        this.nameOz = nameOz;
        this.nameRu = nameRu;
        this.descriptionUz = descriptionUz;
        this.descriptionOz = descriptionOz;
        this.descriptionRu = descriptionRu;
        this.lessonIcon = lessonIcon;
        this.lessonQuestionCount = lessonQuestionCount;
        this.lessonViewsCount = lessonViewsCount;
        this.questions = questions;
    }
}
