package com.jisung.paymentsystem.discount.application;

import com.jisung.paymentsystem.discount.domain.DiscountPolicy;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import com.jisung.paymentsystem.member.domain.MemberGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscountCalculator {
    private final List<DiscountPolicy> discountPolicies;
    public DiscountResult calculate(MemberGrade grade, Long originalAmount) {
        return discountPolicies.stream()
                .filter(policy -> policy.isApplicable(grade)) // 멤버 등급으로 해당 할인 정책이 가능한지 확인
                .findFirst() // 가능한 정책 반환
                .map(policy -> policy.apply(originalAmount)) // 해당 정책의 할인 적용
                .orElse(DiscountResult.none()); // 가능한 정책이 없다면 none 정책 (할인X)
    }

}
