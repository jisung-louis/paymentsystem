package com.jisung.paymentsystem.discount.domain;

import com.jisung.paymentsystem.member.domain.MemberGrade;

public interface DiscountPolicy {

    // 해당 할인정책 적용가능여부 확인 (멤버등급 대조)
    boolean isApplicable(MemberGrade memberGrade);

    // 해당 할인정책 적용 함수 (각 할인 정책대로 구현)
    DiscountResult apply(Long originalAmount);
}
