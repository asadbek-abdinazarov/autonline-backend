package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.SimpleUserDto;
import uz.javachi.autonline.exceptions.CustomException;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.List;
import java.util.Optional;

import static uz.javachi.autonline.utils.Utils.*;
import static uz.javachi.autonline.utils.Utils.buildJwtResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MessageService ms;

    @Transactional(readOnly = true)
    public SimpleUserDto getCurrentUser() {
        Integer currentUserIdOrThrow = SecurityUtils.getCurrentUserIdOrThrow();

        Optional<User> userOptional = userRepository.findById(currentUserIdOrThrow);

        if (userOptional.isEmpty()) {
            throw new CustomException(ms.get("user.not.found"), new Throwable(HttpStatus.NOT_FOUND.name()));
        }
        User user = userOptional.get();

        Subscription subscription = user.getSubscription();


        List<String> roles = getRoles(user);
        List<String> permissions = new java.util.ArrayList<>(getActivePermissionNames(subscription));

        return buildJwtResponse(user, subscription, permissions, roles);
    }
}
