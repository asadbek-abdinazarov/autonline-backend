package uz.javachi.autonline.dto.request;

import lombok.Data;

@Data
public class NewsRequestDTO {

    private String newsTitle;

    private String newsDescription;

    private String newsPhoto;

    private Boolean isActive;
}
