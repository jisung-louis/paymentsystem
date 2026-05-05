package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.member.domain.MemberGrade;
import org.springframework.stereotype.Component;

@Component
public class VipDiscountPolicy implements DiscountPolicy{
    // VIP 정책 : 1000원 고정 할인
    private static final Long DISCOUNT_AMOUNT = 1_000L;

    @Override
    public boolean isApplicable(MemberGrade memberGrade) {
        return memberGrade == MemberGrade.VIP;
    }

    @Override
    public DiscountResult apply(Long originalAmount) {
        Long discountAmount = Math.min(DISCOUNT_AMOUNT, originalAmount); // 상품원가가 고정할인가격 보다 낮으면 해당 상품원가만큼만
        return DiscountResult.fixed(
                discountAmount
        );
    }
}
