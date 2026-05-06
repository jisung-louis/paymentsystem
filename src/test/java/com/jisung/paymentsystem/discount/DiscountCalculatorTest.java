package com.jisung.paymentsystem.discount;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.discount.application.DiscountCalculator;
import com.jisung.paymentsystem.discount.domain.DiscountCalculationResult;
import com.jisung.paymentsystem.discount.domain.DiscountContext;
import com.jisung.paymentsystem.discount.domain.DiscountPolicy;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.discount.domain.DiscountType;
import com.jisung.paymentsystem.discount.domain.PointPaymentDiscountPolicy;
import com.jisung.paymentsystem.discount.domain.VipDiscountPolicy;
import com.jisung.paymentsystem.discount.domain.VvipDiscountPolicy;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import com.jisung.paymentsystem.payment.domain.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountCalculatorTest {

    private final DiscountCalculator discountCalculator = new DiscountCalculator(
            List.of(
                    new PointPaymentDiscountPolicy(),
                    new VipDiscountPolicy(),
                    new VvipDiscountPolicy()
            )
    );

    @Test
    @DisplayName("NORMAL 회원은 할인이 적용되지 않음")
    void normalMemberHasNoDiscount() {
        // given
        DiscountContext context = new DiscountContext(
                MemberGrade.NORMAL,
                PaymentMethod.CREDIT_CARD,
                10_000L
        );

        // when
        DiscountCalculationResult result = discountCalculator.calculate(context);

        // then
        assertThat(result.originalAmount()).isEqualTo(10_000L);
        assertThat(result.totalDiscountAmount()).isEqualTo(0L);
        assertThat(result.finalAmount()).isEqualTo(10_000L);
        assertThat(result.discountResults()).isEmpty();
    }

    @Test
    @DisplayName("VIP 회원은 1000원 고정 할인이 적용됨")
    void vipMemberGetsFixedAmountDiscount() {
        // given
        DiscountContext context = new DiscountContext(
                MemberGrade.VIP,
                PaymentMethod.CREDIT_CARD,
                10_000L
        );

        // when
        DiscountCalculationResult result = discountCalculator.calculate(context);

        // then
        assertThat(result.totalDiscountAmount()).isEqualTo(1_000L);
        assertThat(result.finalAmount()).isEqualTo(9_000L);

        DiscountResult discountResult = result.discountResults().get(0);
        assertThat(discountResult.policyName()).isEqualTo(VipDiscountPolicy.POLICY_NAME);
        assertThat(discountResult.discountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        assertThat(discountResult.discountRate()).isEqualTo(0L);
        assertThat(discountResult.discountAmount()).isEqualTo(1_000L);
        assertThat(discountResult.appliedOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("VVIP 회원은 주문금액의 10% 할인이 적용됨")
    void vvipMemberGetsRateDiscount() {
        // given
        DiscountContext context = new DiscountContext(
                MemberGrade.VVIP,
                PaymentMethod.CREDIT_CARD,
                10_000L
        );

        // when
        DiscountCalculationResult result = discountCalculator.calculate(context);

        // then
        assertThat(result.totalDiscountAmount()).isEqualTo(1_000L);
        assertThat(result.finalAmount()).isEqualTo(9_000L);

        DiscountResult discountResult = result.discountResults().get(0);
        assertThat(discountResult.policyName()).isEqualTo(VvipDiscountPolicy.POLICY_NAME);
        assertThat(discountResult.discountType()).isEqualTo(DiscountType.RATE);
        assertThat(discountResult.discountRate()).isEqualTo(10L);
        assertThat(discountResult.discountAmount()).isEqualTo(1_000L);
        assertThat(discountResult.appliedOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("VIP 할인 금액은 주문금액을 초과하지 않음")
    void vipDiscountDoesNotExceedOriginalAmount() {
        // given
        DiscountContext context = new DiscountContext(
                MemberGrade.VIP,
                PaymentMethod.CREDIT_CARD,
                500L
        );

        // when
        DiscountCalculationResult result = discountCalculator.calculate(context);

        // then
        assertThat(result.totalDiscountAmount()).isEqualTo(500L);
        assertThat(result.finalAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("포인트 결제 시 등급 할인 후 남은 금액에서 5% 중복 할인이 적용됨")
    void pointPaymentDiscountIsAppliedAfterMemberGradeDiscount() {
        // given
        DiscountContext context = new DiscountContext(
                MemberGrade.VVIP,
                PaymentMethod.POINT,
                20_000L
        );

        // when
        DiscountCalculationResult result = discountCalculator.calculate(context);

        // then
        assertThat(result.totalDiscountAmount()).isEqualTo(2_900L);
        assertThat(result.finalAmount()).isEqualTo(17_100L);
        assertThat(result.discountResults()).hasSize(2);

        DiscountResult gradeDiscount = result.discountResults().get(0);
        assertThat(gradeDiscount.policyName()).isEqualTo(VvipDiscountPolicy.POLICY_NAME);
        assertThat(gradeDiscount.discountAmount()).isEqualTo(2_000L);
        assertThat(gradeDiscount.amountBeforeDiscount()).isEqualTo(20_000L);
        assertThat(gradeDiscount.amountAfterDiscount()).isEqualTo(18_000L);
        assertThat(gradeDiscount.appliedOrder()).isEqualTo(1);

        DiscountResult pointDiscount = result.discountResults().get(1);
        assertThat(pointDiscount.policyName()).isEqualTo(PointPaymentDiscountPolicy.POLICY_NAME);
        assertThat(pointDiscount.discountAmount()).isEqualTo(900L);
        assertThat(pointDiscount.amountBeforeDiscount()).isEqualTo(18_000L);
        assertThat(pointDiscount.amountAfterDiscount()).isEqualTo(17_100L);
        assertThat(pointDiscount.appliedOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("동시에 적용되는 정책의 우선순위가 같으면 예외가 발생함")
    void duplicatedPriorityIsRejected() {
        // given
        DiscountPolicy duplicatedVipPolicy = new DiscountPolicy() {
            @Override
            public String policyName() {
                return "DUPLICATED_VIP_DISCOUNT";
            }

            @Override
            public int priority() {
                return VipDiscountPolicy.PRIORITY;
            }

            @Override
            public boolean isApplicable(DiscountContext context) {
                return context.memberGrade() == MemberGrade.VIP;
            }

            @Override
            public DiscountResult apply(DiscountContext context, Long currentAmount) {
                return DiscountResult.fixed(policyName(), currentAmount, 100L);
            }
        };

        DiscountCalculator calculator = new DiscountCalculator(
                List.of(new VipDiscountPolicy(), duplicatedVipPolicy)
        );
        DiscountContext context = new DiscountContext(
                MemberGrade.VIP,
                PaymentMethod.CREDIT_CARD,
                10_000L
        );

        // when & then
        assertThatThrownBy(() -> calculator.calculate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATED_DISCOUNT_POLICY_PRIORITY.getMessage());
    }

}
