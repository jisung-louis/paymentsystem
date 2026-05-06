package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import com.jisung.paymentsystem.payment.domain.PaymentMethod;

public record DiscountContext(
        MemberGrade memberGrade,
        PaymentMethod paymentMethod,
        Long originalAmount
) {
    public DiscountContext {
        if (memberGrade == null) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_CONTEXT);
        }
        if (paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_CONTEXT);
        }
        if (originalAmount == null || originalAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_CONTEXT);
        }
    }
}
