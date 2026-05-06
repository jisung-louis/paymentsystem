package com.jisung.paymentsystem.discount.domain;

public interface DiscountPolicy {

    // 정책명
    String policyName();

    // 우선순위
    int priority();

    // 해당 할인정책 적용가능여부 확인 (멤버등급 대조)
    boolean isApplicable(DiscountContext context);

    // 해당 할인정책 적용 함수 (각 할인 정책대로 구현)
    DiscountResult apply(DiscountContext context, Long currentAmount);
}
