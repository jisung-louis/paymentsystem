package com.jisung.paymentsystem.payment.application;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.discount.application.DiscountCalculator;
import com.jisung.paymentsystem.discount.domain.DiscountCalculationResult;
import com.jisung.paymentsystem.discount.domain.DiscountContext;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import com.jisung.paymentsystem.order.domain.Order;
import com.jisung.paymentsystem.order.infra.OrderRepository;
import com.jisung.paymentsystem.payment.domain.Payment;
import com.jisung.paymentsystem.payment.infra.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final DiscountCalculator discountCalculator;
    private final OrderRepository orderRepository;

    // 결제 서비스 함수
    @Transactional
    public Payment pay(PaymentCommand command){
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        MemberGrade memberGrade = order.getOrderer().getMemberGrade(); // 주문자등급
        Long originalAmount = order.getOriginalAmount(); // 상품원가

        DiscountContext discountContext = new DiscountContext(
                memberGrade,
                command.paymentMethod(),
                originalAmount
        );
        DiscountCalculationResult discountCalculationResult = discountCalculator.calculate(discountContext);

        Payment payment = Payment.create(
                order,
                command.paymentMethod(),
                discountCalculationResult
        );

        return paymentRepository.save(payment);
    }
}
