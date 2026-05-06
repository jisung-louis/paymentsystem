package com.jisung.paymentsystem.payment.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.discount.domain.DiscountCalculationResult;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import com.jisung.paymentsystem.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Column(nullable = false)
    private String orderProductName; // 결제 당시 상품명

    @Column(nullable = false)
    private Long orderOriginalAmount; // 결제 당시 주문 원가

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade memberGrade; // 결제 당시 회원 등급

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus; // 결제 상태(성공/실패/취소)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // 결제 수단(신용카드/포인트)

    @Column(nullable = false)
    private LocalDateTime paidAt; // 결제일시

    @Column(nullable = false)
    private Long totalDiscountAmount; // 총 할인금액

    @Column(nullable = false)
    private Long finalAmount; // 최종결제금액

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("appliedOrder ASC")
    private List<PaymentDiscountHistory> discountHistories = new ArrayList<>();

    private Payment(Order order, PaymentMethod paymentMethod, DiscountCalculationResult discountCalculationResult, LocalDateTime paidAt) {
        this.order = order;
        this.orderProductName = order.getProductName();
        this.orderOriginalAmount = order.getOriginalAmount();
        this.memberGrade = order.getOrderer().getMemberGrade();
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.totalDiscountAmount = discountCalculationResult.totalDiscountAmount();
        this.finalAmount = discountCalculationResult.finalAmount();
    }

    public static Payment create(Order order, PaymentMethod paymentMethod, DiscountCalculationResult discountCalculationResult) {
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (discountCalculationResult == null || !order.getOriginalAmount().equals(discountCalculationResult.originalAmount())) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountCalculationResult.finalAmount() == null || discountCalculationResult.finalAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_FINAL_AMOUNT);
        }

        Payment payment = new Payment(order, paymentMethod, discountCalculationResult, LocalDateTime.now());
        discountCalculationResult.discountResults()
                .forEach(payment::addDiscountHistory);

        return payment;
    }

    public List<PaymentDiscountHistory> getDiscountHistories() {
        return Collections.unmodifiableList(discountHistories);
    }

    private void addDiscountHistory(DiscountResult discountResult) {
        discountHistories.add(PaymentDiscountHistory.create(this, memberGrade, discountResult));
    }
}
