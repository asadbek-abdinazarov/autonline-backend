package uz.javachi.autonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.SearchResponseUserDTO;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.dto.response.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "phone_number")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    private String fullName;

    @NotBlank(message = "{password.notBlank}")
    @Size(min = 8, message = "{password.length}")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "next_payment_date")
    private LocalDateTime nextPaymentDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "teacher_students",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id", unique = true)
    )
    private List<User> students;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentHistory> paymentHistory;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_subscriptions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_id")
    )
    private Subscription subscription;


    @OneToMany(mappedBy = "user")
    private List<TestResult> testResults;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName) && role.getIsActive() && role.isDeleted());
    }

    public boolean hasPermission(String permissionName) {
        return this.roles.stream()
                .filter(role -> role.getIsActive() && role.isDeleted())
                .anyMatch(role -> role.hasPermission(permissionName));
    }

    public boolean isAccountActive() {
        return !this.isActive || this.isDeleted();
    }

    public static UserResponseDTO toDto(User user) {
        return UserResponseDTO.builder()
                .id(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .nextPaymentDate(user.getNextPaymentDate())
                .subscription(Subscription.subscriptionToDto(user.getSubscription()))
                .roles(user.getRoles().stream().map(Role::rolesToDto).toList())
                .paymentHistory(user.getPaymentHistory().stream().map(PaymentHistory::toDto).toList())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static SearchResponseUserDTO searchUserDTO(User user) {
        return SearchResponseUserDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .build();
    }

    public static StudentsResponseToTeacherDTO studentToDtoForTeacher(User user) {
        return StudentsResponseToTeacherDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .nextPaymentDate(user.getNextPaymentDate())
                .subscription(Subscription.simpleSubscriptionDto(user.getSubscription()))
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }


    public static User studentToUserForTeacher(TeacherRegisterStudentRequest dto) {
        return User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .phoneNumber(dto.getPhoneNumber())
                .nextPaymentDate(dto.getNextPaymentDate())
                .build();

    }

}
