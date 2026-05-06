package com.jisung.paymentsystem.discount.application;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.discount.domain.DiscountCalculationResult;
import com.jisung.paymentsystem.discount.domain.DiscountContext;
import com.jisung.paymentsystem.discount.domain.DiscountPolicy;
import com.jisung.paymentsystem.discount.domain.DiscountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DiscountCalculator {
    private final List<DiscountPolicy> discountPolicies;

    public DiscountCalculationResult calculate(DiscountContext context) {
        // [1] 적용가능한 할인정책 찾아서 우선순위대로 정렬
        List<DiscountPolicy> applicablePolicies = discountPolicies.stream()
                .filter(policy -> policy.isApplicable(context))
                .sorted(Comparator.comparingInt(DiscountPolicy::priority))
                .toList();

        // [2] 해당 정책들의 우선순위가 동일하진 않은지 검증
        validateDuplicatedPriority(applicablePolicies);

        // [3] 각 할인정책별로 할인 적용
        Long currentAmount = context.originalAmount();
        List<DiscountResult> discountResults = new ArrayList<>();

        for (int i = 0; i < applicablePolicies.size(); i++) {
            DiscountPolicy policy = applicablePolicies.get(i);
            DiscountResult discountResult = policy.apply(context, currentAmount)
                    .withAppliedOrder(i + 1);

            discountResults.add(discountResult);
            currentAmount = discountResult.amountAfterDiscount();
        }

        // [4] 할인결과 반환 (반환되는값 : 상품원가, 총 할인된금액, 최종가격, 할인된내용들)
        return new DiscountCalculationResult(
                context.originalAmount(),
                context.originalAmount() - currentAmount,
                currentAmount,
                discountResults
        );
    }

    // 우선순위 동일하지 않은지 체크 (동일하면 에러)
    private void validateDuplicatedPriority(List<DiscountPolicy> applicablePolicies) {
        Set<Integer> priorities = new HashSet<>();
        for (DiscountPolicy policy : applicablePolicies) {
            if (!priorities.add(policy.priority())) {
                throw new BusinessException(ErrorCode.DUPLICATED_DISCOUNT_POLICY_PRIORITY);
            }
        }
    }
}
