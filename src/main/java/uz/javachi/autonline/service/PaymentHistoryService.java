package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.response.PaymentHistoryResponse;
import uz.javachi.autonline.repository.PaymentHistoryRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    public List<PaymentHistoryResponse> getAllMyPaymentHistory() {
        Integer currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new RuntimeException("User not authenticated!"));

        return paymentHistoryRepository.findAllByUserId(currentUserId);
    }
}
