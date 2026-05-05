package com.jisung.paymentsystem.payment.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 식별자

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // 주문 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus; // 결제 상태(성공/실패/취소)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // 결제 수단(신용카드/포인트)

    @Column(nullable = false)
    private LocalDateTime paidAt; // 결제일시

    @Column(nullable = false)
    private Long finalAmount; // 최종결제금액

    private Payment(Order order, PaymentMethod paymentMethod, Long finalAmount, LocalDateTime paidAt) {
        this.order = order;
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.finalAmount = finalAmount;
    }

    public static Payment create(Order order, PaymentMethod paymentMethod, Long finalAmount) {
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (finalAmount == null || finalAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_FINAL_AMOUNT);
        }

        return new Payment(order, paymentMethod, finalAmount, LocalDateTime.now());
    }
}
