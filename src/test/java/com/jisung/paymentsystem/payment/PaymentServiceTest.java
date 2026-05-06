package com.jisung.paymentsystem.payment;

import com.jisung.paymentsystem.discount.application.DiscountCalculator;
import com.jisung.paymentsystem.discount.domain.DiscountCalculationResult;
import com.jisung.paymentsystem.discount.domain.DiscountContext;
import com.jisung.paymentsystem.discount.domain.DiscountPolicy;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.discount.domain.DiscountType;
import com.jisung.paymentsystem.discount.domain.PointPaymentDiscountPolicy;
import com.jisung.paymentsystem.discount.domain.VvipDiscountPolicy;
import com.jisung.paymentsystem.member.domain.Member;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import com.jisung.paymentsystem.member.infra.MemberRepository;
import com.jisung.paymentsystem.order.domain.Order;
import com.jisung.paymentsystem.order.infra.OrderRepository;
import com.jisung.paymentsystem.payment.application.PaymentCommand;
import com.jisung.paymentsystem.payment.application.PaymentService;
import com.jisung.paymentsystem.payment.domain.Payment;
import com.jisung.paymentsystem.payment.domain.PaymentDiscountHistory;
import com.jisung.paymentsystem.payment.domain.PaymentMethod;
import com.jisung.paymentsystem.payment.domain.PaymentStatus;
import com.jisung.paymentsystem.payment.infra.PaymentDiscountHistoryRepository;
import com.jisung.paymentsystem.payment.infra.PaymentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentDiscountHistoryRepository paymentDiscountHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("포인트 결제 성공 시 결제 스냅샷과 할인 이력을 저장함")
    void payWithPointStoresPaymentSnapshotAndDiscountHistories() {
        // given
        Order order = saveOrder(MemberGrade.VVIP, "keyboard", 20_000L);

        // when
        Payment payment = paymentService.pay(new PaymentCommand(order.getId(), PaymentMethod.POINT));
        entityManager.flush();
        entityManager.clear();

        // then
        Payment savedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        List<PaymentDiscountHistory> histories = paymentDiscountHistoryRepository.findByPaymentIdOrderByAppliedOrderAsc(savedPayment.getId());

        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(savedPayment.getPaidAt()).isNotNull();
        assertThat(savedPayment.getOrderProductName()).isEqualTo("keyboard");
        assertThat(savedPayment.getOrderOriginalAmount()).isEqualTo(20_000L);
        assertThat(savedPayment.getMemberGrade()).isEqualTo(MemberGrade.VVIP);
        assertThat(savedPayment.getTotalDiscountAmount()).isEqualTo(2_900L);
        assertThat(savedPayment.getFinalAmount()).isEqualTo(17_100L);

        assertThat(histories).hasSize(2);
        assertHistory(
                histories.get(0),
                VvipDiscountPolicy.POLICY_NAME,
                DiscountType.RATE,
                10L,
                2_000L,
                20_000L,
                18_000L,
                1
        );
        assertHistory(
                histories.get(1),
                PointPaymentDiscountPolicy.POLICY_NAME,
                DiscountType.RATE,
                5L,
                900L,
                18_000L,
                17_100L,
                2
        );
    }

    @Test
    @DisplayName("정책 변경/삭제로 재계산 결과가 달라져도 과거 결제 데이터와 할인 이력은 보존됨")
    void paymentHistoryIsPreservedAfterPolicyChangeOrDeletion() {
        // given
        Order order = saveOrder(MemberGrade.VVIP, "monitor", 20_000L);
        Payment payment = paymentService.pay(new PaymentCommand(order.getId(), PaymentMethod.POINT));
        Long paymentId = payment.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        DiscountCalculationResult recalculatedResult = new DiscountCalculator(
                List.of(new ChangedVvipDiscountPolicy(), new PointPaymentDiscountPolicy())
        ).calculate(new DiscountContext(MemberGrade.VVIP, PaymentMethod.POINT, 20_000L));
        DiscountCalculationResult recalculatedAfterPolicyDeletion = new DiscountCalculator(
                List.of()
        ).calculate(new DiscountContext(MemberGrade.VVIP, PaymentMethod.POINT, 20_000L));

        // then
        assertThat(recalculatedResult.finalAmount()).isEqualTo(9_500L);
        assertThat(recalculatedAfterPolicyDeletion.finalAmount()).isEqualTo(20_000L);

        Payment savedPayment = paymentRepository.findById(paymentId).orElseThrow();
        List<PaymentDiscountHistory> histories = paymentDiscountHistoryRepository.findByPaymentIdOrderByAppliedOrderAsc(paymentId);

        assertThat(savedPayment.getOrderProductName()).isEqualTo("monitor");
        assertThat(savedPayment.getOrderOriginalAmount()).isEqualTo(20_000L);
        assertThat(savedPayment.getMemberGrade()).isEqualTo(MemberGrade.VVIP);
        assertThat(savedPayment.getTotalDiscountAmount()).isEqualTo(2_900L);
        assertThat(savedPayment.getFinalAmount()).isEqualTo(17_100L);

        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getPolicyName()).isEqualTo(VvipDiscountPolicy.POLICY_NAME);
        assertThat(histories.get(0).getDiscountRate()).isEqualTo(10L);
        assertThat(histories.get(0).getDiscountAmount()).isEqualTo(2_000L);
        assertThat(histories.get(1).getPolicyName()).isEqualTo(PointPaymentDiscountPolicy.POLICY_NAME);
        assertThat(histories.get(1).getDiscountRate()).isEqualTo(5L);
        assertThat(histories.get(1).getDiscountAmount()).isEqualTo(900L);
    }

    private Order saveOrder(MemberGrade memberGrade, String productName, Long originalAmount) {
        Member member = memberRepository.save(Member.create(memberGrade));
        return orderRepository.save(Order.create(productName, originalAmount, member));
    }

    private void assertHistory(
            PaymentDiscountHistory history,
            String policyName,
            DiscountType discountType,
            Long discountRate,
            Long discountAmount,
            Long amountBeforeDiscount,
            Long amountAfterDiscount,
            Integer appliedOrder
    ) {
        assertThat(history.getMemberGrade()).isEqualTo(MemberGrade.VVIP);
        assertThat(history.getPolicyName()).isEqualTo(policyName);
        assertThat(history.getDiscountType()).isEqualTo(discountType);
        assertThat(history.getDiscountRate()).isEqualTo(discountRate);
        assertThat(history.getDiscountAmount()).isEqualTo(discountAmount);
        assertThat(history.getAmountBeforeDiscount()).isEqualTo(amountBeforeDiscount);
        assertThat(history.getAmountAfterDiscount()).isEqualTo(amountAfterDiscount);
        assertThat(history.getAppliedOrder()).isEqualTo(appliedOrder);
    }

    private static class ChangedVvipDiscountPolicy implements DiscountPolicy {
        private static final Long DISCOUNT_RATE = 50L;

        @Override
        public String policyName() {
            return "CHANGED_VVIP_RATE_DISCOUNT";
        }

        @Override
        public int priority() {
            return VvipDiscountPolicy.PRIORITY;
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
}
