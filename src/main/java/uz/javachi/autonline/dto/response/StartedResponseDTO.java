package uz.javachi.autonline.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartedResponseDTO {
    private Long testResultId;
    private Integer testTemplateId;
    private String status;
}
