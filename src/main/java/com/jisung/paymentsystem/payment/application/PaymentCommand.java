package com.jisung.paymentsystem.payment.application;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.payment.domain.PaymentMethod;

public record PaymentCommand(
        // 결제 기능에 필요한 정보: 주문 정보,  결제 수단
        // "최종결제금액", "결제일시"는 '결제'기능에서 계산해야 할 내용이다.
        Long orderId,
        PaymentMethod paymentMethod
) {
    public PaymentCommand {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_COMMAND);
        }
        if (paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }
}
