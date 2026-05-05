package com.jisung.paymentsystem.discount.domain;

public record DiscountResult(
        DiscountType discountType,  // 할인종류
        Long discountRate,          // 할인률
        Long discountAmount         // 할인금액
) {
    public DiscountResult {
        if (discountType == null) {
            throw new IllegalArgumentException("할인종류는 null일 수 없습니다.");
        }
        if (discountRate == null || discountRate < 0) {
            throw new IllegalArgumentException("할인률은 0 이상이어야 합니다.");
        }
        if (discountAmount == null || discountAmount < 0) {
            throw new IllegalArgumentException("할인금액은 0 이상이어야 합니다.");
        }
    }

    // 할인 없음
    public static DiscountResult none() {
        return new DiscountResult(DiscountType.NONE, 0L, 0L);
    }

    // 고정금액 할인
    public static DiscountResult fixed(Long discountAmount) {
        return new DiscountResult(DiscountType.FIXED_AMOUNT, 0L, discountAmount);
    }

    // 비율 할인
    public static DiscountResult rate(Long discountRate, Long discountAmount) {
        return new DiscountResult(DiscountType.RATE, discountRate, discountAmount);
    }
}
