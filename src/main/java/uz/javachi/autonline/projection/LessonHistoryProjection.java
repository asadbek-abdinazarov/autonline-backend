package uz.javachi.autonline.projection;

import java.time.LocalDateTime;

public interface LessonHistoryProjection {
    Integer getLessonHistoryId();

    Integer getPercentage();

    Integer getCorrectAnswersCount();

    Integer getNotCorrectAnswersCount();

    Integer getAllQuestionCount();

    LocalDateTime getCreatedDate();

    String getLessonName();
}
