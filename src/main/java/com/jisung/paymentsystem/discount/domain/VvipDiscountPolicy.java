package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.member.domain.MemberGrade;
import org.springframework.stereotype.Component;

@Component
public class VvipDiscountPolicy implements DiscountPolicy{
    // VVIP 할인 정책 : 10% 할인
    private static final Long DISCOUNT_RATE = 10L;

    @Override
    public boolean isApplicable(MemberGrade memberGrade) {
        return memberGrade == MemberGrade.VVIP;
    }

    @Override
    public DiscountResult apply(Long originalAmount) {
        Long discountAmount = originalAmount * DISCOUNT_RATE / 100;
        return DiscountResult.rate(
                DISCOUNT_RATE,
                discountAmount
        );
    }
}
