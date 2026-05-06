package com.jisung.paymentsystem.payment.infra;

import com.jisung.paymentsystem.payment.domain.PaymentDiscountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentDiscountHistoryRepository extends JpaRepository<PaymentDiscountHistory, Long> {
    // 결제할인이력을 우선순위 오름차순으로 가져옴
    List<PaymentDiscountHistory> findByPaymentIdOrderByAppliedOrderAsc(Long paymentId);
}
