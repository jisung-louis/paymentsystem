package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PointPaymentDiscountPolicy implements DiscountPolicy {
    // POINT결제 정책 : 5% 할인
    public static final String POLICY_NAME = "POINT_PAYMENT_RATE_DISCOUNT";
    public static final int PRIORITY = 200;
    private static final Long DISCOUNT_RATE = 5L;

    @Override
    public String policyName() {
        return POLICY_NAME;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public boolean isApplicable(DiscountContext context) {
        return context.paymentMethod() == PaymentMethod.POINT;
    }

    @Override
    public DiscountResult apply(DiscountContext context, Long currentAmount) {
        Long discountAmount = currentAmount * DISCOUNT_RATE / 100;
        return DiscountResult.rate(
                policyName(),
                DISCOUNT_RATE,
                currentAmount,
                discountAmount
        );
    }
}
