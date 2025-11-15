package uz.javachi.autonline.projection;

public interface LessonAnonsProjection {
    Integer getLessonId();
    String getNameUz();
    String getNameOz();
    String getNameRu();
    String getDescriptionUz();
    String getDescriptionOz();
    String getDescriptionRu();
    String getLessonIcon();
    Long getLessonQuestionCount();
    Long getLessonViewsCount();
}
