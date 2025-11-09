package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.BlockNotification;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void blockUser(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
            if (user.isAccountActive()) {
                throw new RuntimeException("Account is already inactive.");
            }
            user.setIsActive(false);
            userRepository.saveAndFlush(user);
            log.warn("User with ID {} has been blocked.", userId);
        } finally {
            messagingTemplate.convertAndSend("/topic/user-block/%s".formatted(userId),
                    new BlockNotification(userId, "Your account has been blocked!"));
            log.warn("Block notification sent to user with ID {}.", userId);
        }
    }
}
