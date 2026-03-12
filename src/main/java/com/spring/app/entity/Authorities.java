package com.spring.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_authorities",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"memberid", "authority"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authorities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_seq")
    @SequenceGenerator(
            name = "auth_seq",
            sequenceName = "seq_tbl_authorities",
            allocationSize = 1   // SQL 에서 INCREMENT BY 1 이므로 allocationSize = 1 로 해야 한다. 
    )
    @Column(name = "num")
    private Long num;

    @Column(name = "authority", length = 50, nullable = false)
    private String authority;

    // 연관관계 정의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", nullable = false)    
    private Member member;
    // tbl_authorities.memberid 컬럼을 
    // JPA가 Member 엔티티 객체와의 연관관계로 직접 관리해준다.
    // tbl_authorities.memberid 컬럼에 입력되거나 수정되어지는 FK 값은 JPA가 알아서 자동으로 관리해준다. 
    
}

