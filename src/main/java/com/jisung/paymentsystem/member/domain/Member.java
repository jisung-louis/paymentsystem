package com.jisung.paymentsystem.member.domain;

import com.jisung.paymentsystem.common.exception.BusinessException;
import com.jisung.paymentsystem.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade memberGrade; // NORMAL, VIP, VVIP

    private Member(MemberGrade memberGrade) {
        this.memberGrade = memberGrade;
    }

    public static Member create(MemberGrade memberGrade) {
        if (memberGrade == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_GRADE);
        }

        return new Member(memberGrade);
    }
}
