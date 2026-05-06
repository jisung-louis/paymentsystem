package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.member.domain.MemberGrade;
import org.springframework.stereotype.Component;

@Component
public class VvipDiscountPolicy implements DiscountPolicy {
    // VVIP 할인 정책 : 10% 할인
    public static final String POLICY_NAME = "VVIP_RATE_DISCOUNT";
    public static final int PRIORITY = 100;
    private static final Long DISCOUNT_RATE = 10L;

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
        return context.memberGrade() == MemberGrade.VVIP;
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
