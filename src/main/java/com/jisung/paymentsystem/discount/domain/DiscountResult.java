package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;

public record DiscountResult(
        String policyName,          // 적용 정책명
        DiscountType discountType,  // 할인종류
        Long discountRate,          // 할인율
        Long discountAmount,        // 할인금액
        Long amountBeforeDiscount,  // 할인 적용 전 금액
        Long amountAfterDiscount,   // 할인 적용 후 금액
        Integer appliedOrder        // 할인 적용 순서
) {
    public DiscountResult {
        if (policyName == null || policyName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountType == null) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountRate == null || discountRate < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountAmount == null || discountAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (amountBeforeDiscount == null || amountBeforeDiscount < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (amountAfterDiscount == null || amountAfterDiscount < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountAmount > amountBeforeDiscount) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (!amountBeforeDiscount.equals(discountAmount + amountAfterDiscount)) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (appliedOrder != null && appliedOrder <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
    }

    public static DiscountResult fixed(String policyName, Long amountBeforeDiscount, Long discountAmount) {
        return new DiscountResult(
                policyName,
                DiscountType.FIXED_AMOUNT,
                0L,
                discountAmount,
                amountBeforeDiscount,
                amountBeforeDiscount - discountAmount,
                null
        );
    }

    public static DiscountResult rate(String policyName, Long discountRate, Long amountBeforeDiscount, Long discountAmount) {
        return new DiscountResult(
                policyName,
                DiscountType.RATE,
                discountRate,
                discountAmount,
                amountBeforeDiscount,
                amountBeforeDiscount - discountAmount,
                null
        );
    }

    public DiscountResult withAppliedOrder(Integer appliedOrder) {
        return new DiscountResult(
                policyName,
                discountType,
                discountRate,
                discountAmount,
                amountBeforeDiscount,
                amountAfterDiscount,
                appliedOrder
        );
    }
}
