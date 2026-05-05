package com.jisung.paymentsystem.order.infra;

import com.jisung.paymentsystem.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
