package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.javachi.autonline.dto.response.PaymentHistoryResponse;
import uz.javachi.autonline.model.PaymentHistory;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Integer> {
    @Query("SELECT new uz.javachi.autonline.dto.response.PaymentHistoryResponse(" +
            "p.paymentAmount, p.paymentCurrency, p.isPaid, p.paymentDate, " +
            "p.paymentMethod, p.description) " +
            "FROM PaymentHistory p WHERE p.user.userId = :userId AND p.deletedAt IS NULL")
    List<PaymentHistoryResponse> findAllByUserId(@Param("userId") Integer userId);

    Long countByIsPaid(Boolean isPaid);
}