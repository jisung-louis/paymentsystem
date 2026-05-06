package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;

import java.util.List;

public record DiscountCalculationResult(
        Long originalAmount,
        Long totalDiscountAmount,
        Long finalAmount,
        List<DiscountResult> discountResults
) {
    public DiscountCalculationResult {
        if (originalAmount == null || originalAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (totalDiscountAmount == null || totalDiscountAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (finalAmount == null || finalAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (discountResults == null) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }
        if (!originalAmount.equals(totalDiscountAmount + finalAmount)) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RESULT);
        }

        discountResults = List.copyOf(discountResults);
    }
}
