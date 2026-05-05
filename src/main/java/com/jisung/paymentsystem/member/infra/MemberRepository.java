package com.jisung.paymentsystem.member.infra;

import com.jisung.paymentsystem.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
