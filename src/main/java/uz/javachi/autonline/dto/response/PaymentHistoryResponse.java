package uz.javachi.autonline.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class PaymentHistoryResponse {

    private BigDecimal paymentAmount;

    private String paymentCurrency;

    private Boolean isPaid;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private String description;


    public PaymentHistoryResponse(BigDecimal paymentAmount, String paymentCurrency,
                                 Boolean isPaid, LocalDateTime paymentDate, 
                                 String paymentMethod, String description) {
        this.paymentAmount = paymentAmount;
        this.paymentCurrency = paymentCurrency;
        this.isPaid = isPaid;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }
}
