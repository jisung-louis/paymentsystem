package com.jisung.paymentsystem.payment.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.discount.domain.DiscountType;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "payment_discount_histories")
public class PaymentDiscountHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment; // 결제정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade memberGrade; // 결제 당시 회원등급

    @Column(nullable = false)
    private String policyName; // 적용 정책명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // 할인종류

    @Column(nullable = false)
    private Long discountRate; // 할인율

    @Column(nullable = false)
    private Long discountAmount; // 할인금액

    @Column(nullable = false)
    private Long amountBeforeDiscount; // 할인적용 전 금액

    @Column(nullable = false)
    private Long amountAfterDiscount; // 할인적용 후 금액

    @Column(nullable = false)
    private Integer appliedOrder; // 할인적용 순서

    private PaymentDiscountHistory(Payment payment, MemberGrade memberGrade, DiscountResult discountResult) {
        this.payment = payment;
        this.memberGrade = memberGrade;
        this.policyName = discountResult.policyName();
        this.discountType = discountResult.discountType();
        this.discountRate = discountResult.discountRate();
        this.discountAmount = discountResult.discountAmount();
        this.amountBeforeDiscount = discountResult.amountBeforeDiscount();
        this.amountAfterDiscount = discountResult.amountAfterDiscount();
        this.appliedOrder = discountResult.appliedOrder();
    }

    public static PaymentDiscountHistory create(Payment payment, MemberGrade memberGrade, DiscountResult discountResult) {
        if (payment == null || memberGrade == null || discountResult == null || discountResult.appliedOrder() == null) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }

        return new PaymentDiscountHistory(payment, memberGrade, discountResult);
    }
}
