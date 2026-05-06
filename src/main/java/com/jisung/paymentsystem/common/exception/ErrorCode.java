package com.jisung.paymentsystem.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // PAYMENT 관련 에러코드
    ORDER_NOT_FOUND("주문을 찾을수 없습니다."),
    INVALID_PAYMENT_COMMAND("결제 요청값이 올바르지 않습니다."),
    INVALID_PAYMENT_METHOD("결제수단은 필수입니다."),
    INVALID_FINAL_AMOUNT("최종 결제금액은 0원 이상이어야 합니다."),
    INVALID_DISCOUNT_CONTEXT("할인 계산 요청값이 올바르지 않습니다."),
    INVALID_DISCOUNT_RESULT("할인 계산 결과가 올바르지 않습니다."),
    DUPLICATED_DISCOUNT_POLICY_PRIORITY("동일 우선순위의 할인 정책이 동시에 적용될 수 없습니다."),

    // ORDER 관련 에러코드
    INVALID_ORDER_AMOUNT("주문 금액은 0보다 커야 합니다."),
    INVALID_PRODUCT_NAME("상품명은 필수입니다."),
    INVALID_ORDERER("주문자는 필수입니다."),

    // MEMBER 관련 에러코드
    INVALID_MEMBER_GRADE("회원 등급은 필수입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

}
