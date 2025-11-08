package uz.javachi.autonline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentHistoryResponseDTO {

    private Integer paymentHistoryId;

    private BigDecimal paymentAmount;

    private String paymentCurrency;

    private Boolean isPaid;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
