package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.dto.response.PaymentHistoryResponse;
import uz.javachi.autonline.service.PaymentHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-history")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @GetMapping
    public ResponseEntity<List<PaymentHistoryResponse>> getAllMyPaymentHistory() {
        List<PaymentHistoryResponse> paymentHistory = paymentHistoryService.getAllMyPaymentHistory();
        return ResponseEntity.ok(paymentHistory);
    }
}
