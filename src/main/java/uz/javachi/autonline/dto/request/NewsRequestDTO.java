package uz.javachi.autonline.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NewsRequestDTO {

    private String newsTitle;

    private String newsDescription;

    private MultipartFile newsPhoto;

    private Boolean isActive;
}
