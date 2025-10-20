package uz.javachi.autonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import uz.javachi.autonline.dto.response.NewsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "news_title")
        })
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer newsId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    @Column(name = "news_title", nullable = false, length = 500)
    private String newsTitle;

    @Column(name = "news_description", length = 500)
    private String newsDescription;

    @Column(name = "news_photo")
    private String newsPhoto;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "news_created_at", nullable = false, updatable = false)
    private LocalDateTime newsCreatedAt;


    public static List<NewsResponse> toResponseList(List<News> newsList) {
        return newsList.stream().map(news -> NewsResponse.builder()
                .newsId(news.getNewsId())
                .newsTitle(news.getNewsTitle())
                .newsDescription(news.getNewsDescription())
                .newsPhoto(news.getNewsPhoto())
                .isActive(news.getIsActive())
                .newsCreatedAt(news.getNewsCreatedAt())
                .build()).toList();
    }

}
