package uz.javachi.autonline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answers {
    private int status;

    @JsonProperty("answer_id")
    private int answerId;

    private AnswerText answer;
}
