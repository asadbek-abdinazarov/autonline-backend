package uz.javachi.autonline.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsResponse {
    private Integer newsId;
    private String newsTitle;
    private String newsDescription;
    private String newsPhoto;
    private Boolean isActive;
    private LocalDateTime newsCreatedAt;
}
