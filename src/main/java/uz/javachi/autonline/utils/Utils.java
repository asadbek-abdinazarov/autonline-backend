package uz.javachi.autonline.utils;

import org.springframework.security.crypto.password.PasswordEncoder;
import uz.javachi.autonline.config.Localized;
import uz.javachi.autonline.dto.SimpleUserDto;
import uz.javachi.autonline.dto.request.RegisterRequest;
import uz.javachi.autonline.dto.response.JwtResponse;
import uz.javachi.autonline.dto.response.QuestionResponseDTO;
import uz.javachi.autonline.dto.response.VariantResponseDTO;
import uz.javachi.autonline.model.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static JwtResponse buildJwtResponse(String accessToken, String refreshToken, User user, Subscription subscription, List<String> roles, List<String> permissions, String sessionId) {
        return JwtResponse.builder()
                .id(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .nextPaymentDate(user.getNextPaymentDate())
                .subscription(subscription.getName())
                .subscriptionDefName(subscription.getDefName())
                .sessionId(sessionId)
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    public static SimpleUserDto buildJwtResponse(User user, Subscription subscription, List<String> permissions, List<String> roles) {
        return SimpleUserDto.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .nextPaymentDate(user.getNextPaymentDate())
                .subscription(subscription.getName())
                .subscriptionDefName(subscription.getDefName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public static User buildNewUser(RegisterRequest request, Subscription subscription, Role role, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .isActive(true)
                .subscription(subscription)
                .paymentHistory(new ArrayList<>())
                .roles(Set.of(role))
                .build();
    }

    public static List<String> getActivePermissionNames(Subscription subscription) {
        return subscription.getPermissions().stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()) && !permission.isDeleted())
                .map(Permission::getName)
                .toList();
    }

    public static List<String> getPermissions(User user) {
        return user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getIsActive() && !permission.isDeleted())
                .map(Permission::getName)
                .distinct()
                .toList();
    }

    public static List<String> getRoles(User user) {
        return user.getRoles().stream()
                .filter(role -> role.getIsActive() && !role.isDeleted())
                .map(Role::getName)
                .toList();
    }

    public static List<QuestionResponseDTO> getQuestionResponseDTOS(List<Question> question, String lang) {
        return question.stream()
                .map(q -> {
                    QuestionResponseDTO qdto = new QuestionResponseDTO();
                    qdto.setQuestionId(q.getQuestionId());
                    qdto.setPhoto(q.getPhoto());
                    QuestionTranslation qt = findTranslation(q.getTranslations(), lang);
                    qdto.setQuestionText(qt != null ? qt.getQuestionText() : null);

                    List<VariantResponseDTO> vs = q.getVariants().stream()
                            .map(v -> {
                                VariantResponseDTO vd = new VariantResponseDTO();
                                vd.setVariantId(v.getVariantId());
                                vd.setIsCorrect(v.getIsCorrect());
                                VariantTranslation vt = findTranslation(v.getTranslations(), lang);
                                vd.setText(vt != null ? vt.getText() : null);
                                return vd;
                            }).collect(Collectors.toList());
                    qdto.setVariants(vs);
                    return qdto;
                }).collect(Collectors.toList());
    }

    public static <X extends Localized> X findTranslation(Collection<X> translations, String lang) {
        if (translations == null || translations.isEmpty()) return null;

        return translations.stream()
                .filter(t -> lang.equalsIgnoreCase(t.getLang()))
                .findFirst()
                .orElse(null);
    }

    public static String urlEncoder(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}
