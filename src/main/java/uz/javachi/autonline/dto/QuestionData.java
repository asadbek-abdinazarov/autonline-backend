package uz.javachi.autonline.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionData {
    private int id;

    @JsonProperty("bilet_id")
    private int biletId;

    @JsonProperty("question_id")
    private int questionId;

    private String photo;

    private QuestionText question;
    private Answers answers;
}