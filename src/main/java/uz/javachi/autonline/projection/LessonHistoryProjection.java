package uz.javachi.autonline.projection;

import java.time.LocalDateTime;
//maxsus imkoniyatlar uchun home pageda bir bo'lim yarat u mavzular bo'limidan tepada bo'lsin radnom test ishlash buttoni shu bo'lim ichiga ko'chir
public interface LessonHistoryProjection {
    Integer getLessonHistoryId();

    Integer getPercentage();

    Integer getCorrectAnswersCount();

    Integer getNotCorrectAnswersCount();

    Integer getAllQuestionCount();

    LocalDateTime getCreatedDate();

    String getLessonName();

    String getLessonIcon();
}
