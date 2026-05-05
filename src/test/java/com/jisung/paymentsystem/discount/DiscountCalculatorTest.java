package com.jisung.paymentsystem.discount;

import com.jisung.paymentsystem.discount.application.DiscountCalculator;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.discount.domain.DiscountType;
import com.jisung.paymentsystem.discount.domain.VipDiscountPolicy;
import com.jisung.paymentsystem.discount.domain.VvipDiscountPolicy;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountCalculatorTest {

    private final DiscountCalculator discountCalculator = new DiscountCalculator(
            List.of(
                    new VipDiscountPolicy(),
                    new VvipDiscountPolicy()
            )
    );

    @Test
    @DisplayName("NORMAL 회원은 할인이 적용되지 않음")
    void normalMemberHasNoDiscount() {
        // given
        MemberGrade grade = MemberGrade.NORMAL;
        Long originalAmount = 10_000L;

        // when
        DiscountResult result = discountCalculator.calculate(grade, originalAmount);

        // then
        assertThat(result.discountType()).isEqualTo(DiscountType.NONE);
        assertThat(result.discountRate()).isEqualTo(0L);
        assertThat(result.discountAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("VIP 회원은 1000원 고정 할인이 적용됨")
    void vipMemberGetsFixedAmountDiscount() {
        // given
        MemberGrade grade = MemberGrade.VIP;
        Long originalAmount = 10_000L;

        // when
        DiscountResult result = discountCalculator.calculate(grade, originalAmount);

        // then
        assertThat(result.discountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        assertThat(result.discountRate()).isEqualTo(0L);
        assertThat(result.discountAmount()).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("VVIP 회원은 주문금액의 10% 할인이 적용됨")
    void vvipMemberGetsRateDiscount() {
        // given
        MemberGrade grade = MemberGrade.VVIP;
        Long originalAmount = 10_000L;

        // when
        DiscountResult result = discountCalculator.calculate(grade, originalAmount);

        // then
        assertThat(result.discountType()).isEqualTo(DiscountType.RATE);
        assertThat(result.discountRate()).isEqualTo(10L);
        assertThat(result.discountAmount()).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("VIP 할인 금액은 주문금액을 초과하지 않음")
    void vipDiscountDoesNotExceedOriginalAmount() {
        DiscountResult result = discountCalculator.calculate(MemberGrade.VIP, 500L);

        assertThat(result.discountAmount()).isEqualTo(500L);
    }

}
