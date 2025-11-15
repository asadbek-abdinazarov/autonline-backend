package uz.javachi.autonline.dto.request;

import lombok.Data;

@Data
public class MessageRequestDTO {
    private String msgKey;
    private String msgLang;
    private String message;
}
