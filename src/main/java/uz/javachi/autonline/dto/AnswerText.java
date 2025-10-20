package uz.javachi.autonline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerText {
    private List<String> oz;
    private List<String> uz;
    private List<String> ru;
}
