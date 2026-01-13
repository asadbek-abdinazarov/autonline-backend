package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import uz.javachi.autonline.dto.response.TestTemplateResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer testTemplateId;
    private String title;
    private Integer duration;

    private Integer maxScore;
    private Integer passScore;


    @OneToMany(mappedBy = "testTemplate")
    private List<TestResult> testResults;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
