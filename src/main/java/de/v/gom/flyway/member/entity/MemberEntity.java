package de.v.gom.flyway.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false, columnDefinition = "comment '멤버를 식별하기 위한 식별자로 고유 번호를 갖는다.'")
    private Long memberId;

    @Column(name = "member_name", nullable = false, columnDefinition = "comment '멤버의 이름을 나타낸다.'")
    private String name;
}
