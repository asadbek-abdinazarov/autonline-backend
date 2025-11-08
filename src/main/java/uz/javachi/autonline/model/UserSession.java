package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    private String ipAddress;
    private String userAgent;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime lastActive = OffsetDateTime.now();

    private String status = "ACTIVE";
}

