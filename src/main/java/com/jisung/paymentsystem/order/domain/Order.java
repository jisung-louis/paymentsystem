package com.jisung.paymentsystem.order.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import com.jisung.paymentsystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 식별키

    @Column(nullable = false)
    private String productName; // 상품명

    @Column(nullable = false)
    private Long originalAmount; // 상품 원가

    @ManyToOne
    @JoinColumn(name = "orderer_id", nullable = false)
    private Member orderer; // 주문자

    private Order(String productName, Long originalAmount, Member orderer) {
        this.productName = productName;
        this.originalAmount = originalAmount;
        this.orderer = orderer;
    }

    public static Order create(String productName, Long originalAmount, Member orderer) {
        if (productName == null || productName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_NAME);
        }
        if (originalAmount == null || originalAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_AMOUNT);
        }
        if (orderer == null) {
            throw new BusinessException(ErrorCode.INVALID_ORDERER);
        }

        return new Order(productName, originalAmount, orderer);
    }
}
