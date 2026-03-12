package com.spring.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.spring.app.member.domain.MemberDTO;

@Entity
@Table(name = "tbl_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @Column(name = "memberid", length = 50)
    private String memberid;
    // @Id 는 Primary Key 로 사용되어지는 컬럼임.

    @Column(name = "passwd", length = 200, nullable = false)
    private String passwd;

    @Column(name = "enabled", length = 1, columnDefinition="DEFAULT '1'", nullable = false, 
    		insertable=false, updatable=false) // INSERT, UPDATE 제외
    private String enabled;   // '1' 또는 '0'

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "registerday", columnDefinition="DATE DEFAULT SYSDATE", 
    		insertable=false, updatable=false)
    private LocalDate registerday; 
    /* columnDefinition 은 DB 컬럼의 정보를 직접 주는 것이다. 
       예를 들어 columnDefinition = "Nvarchar2(20) default '사원'" 인 것이다.
       
       insertable = false 로 설정하면 엔티티 저장(insert)시 이 필드를 빼라는 말이다.
       insertable = true 로 설정하면 엔티티 저장(insert)시 이 필드는 들어간다는 말이다.
       기본값은 true 이므로 생략하면 insertable = true 이다. 
       다시 설명하자면, insertable=false 로 했으므로 JPA가 INSERT 시에 이 필드를 쿼리에 포함하지 않는다.
       즉, INSERT INTO members (...) VALUES (...) 쿼리에서 registerday 컬럼은 빼라는 말이다.
       왜냐하면 columnDefinition = "DATE DEFAULT SYSDATE" 으로 했기 때문에
       엔티티 저장(insert)시 registerday 컬럼을 빼더라도 registerday 컬럼에는 DB에서 자동적으로 SYSDATE 가 들어가기 때문이다.

       updatable = false 로 설정하면 JPA가 UPDATE 시에도 이 필드는 수정하지 않겠다는 말이다.
       즉, UPDATE members SET registerday = ? 이런 쿼리는 절대 생성하지 않겠다는 말이다.
       즉, 한번 데이터 입력 후 registerday 컬럼의 값은 수정 불가라는 뜻이다.
    */

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;
    // Spring Boot는 기본적으로 필드명에 카멜 표기를 사용하면 DB 컬럼명을 스네이크 표기(언더바)로 변환함.  

    /*
       한 명의 회원은 여러 개의 권한을 가질 수 있음
       오라클 테이블은 1 : 다 관계임. 
       즉, tbl_member(1) : tbl_authorities(다)  
           memberid(P.K)   memberid(F.K) 
           
       따라서 JPA에서는 
       Member    엔티티에서는  @OneToMany 로
       Authority 엔티티에서는  @ManyToOne 로
       로 구성하면 된다.    
    */
    
    // 연관관계 정의
    // 권한과의 관계
    @OneToMany(mappedBy = "member",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<Authorities> authorities = new ArrayList<>();
    // MemberDTO 에는 private List<String> authorities; 으로 해두었지만
    // Entity 인 Member 에서는 private List<Authority> authorities 로 해야 한다.
    // DTO 는 전달용도/화면에서보여지는용도 이고, 
    // Entity는 DB 매핑용 이라서, 객체 관계(엔티티 관계)를 직접 매핑해야 하기 때문이다.
    // new ArrayList<>(); 로 초기화 하는 이유는 
    // 만약에 초기화를 해주지 않으면 null 로 초기화가 되므로 
    // member.getAuthorities().add(auth) 를 할때 NullPointerException 발생할 가능성이 있다. 
    // 그래서 안전하게 미리 초기화를 해준다.
    
    /*
       mappedBy = "member"
       
       가장 중요하고, 헷갈리는 부분이다.
       
       mappedBy = "member" 는 이 연관관계의 주인은 "Authority 쪽에 있는 member 필드" 이다
       라는 뜻이다. 
       
       Authority 엔티티에 이런 코드가 있다.

	   @ManyToOne(fetch = FetchType.LAZY)
	   @JoinColumn(name = "memberid", nullable = false)
	   private Member member;

       여기서:
       실제 FK 컬럼(memberid)을 관리하는 쪽은 Authority 엔티티 이다.
       따라서 Authority가 연관관계의 주인(owner) 이며
       Member 엔티티는 그 관계를 "읽기만" 한다는 의미이다.
       
       DB에서 FK는 tbl_authorities 테이블에 존재한다.
       즉, FK를 가진 테이블이 실제 관계를 맺은 테이블 이므로 관계의 주인이 되어야 한다.
       그래서:
       Authority 엔티티가 연관관계 주인이 된다.
       Member 엔티티 에서는 mappedBy 이라는 표시를 해줌으로서   
       다른 엔티티의 특정 필드에 의해 Member 엔티티가 참조를 당한다는 표시를 해줄때 쓰이는 것이다. 
    */
    
    /*
       cascade = CascadeType.ALL
       은 
       memberRepository.delete(member); 를 할 때
       
       오라클에서 tbl_authorities 테이블에서 FOREIGN KEY 생성시 
       CONSTRAINT FK_tbl_authorities_memberid  FOREIGN KEY(memberid) REFERENCES tbl_member(memberid) ON DELETE CASCADE 
       으로 하든
       ON DELETE CASCADE 없이
       CONSTRAINT FK_tbl_authorities_memberid  FOREIGN KEY(memberid) REFERENCES tbl_member(memberid)  
       으로 하든지 관계없이 
       모두 
       DELETE FROM tbl_authorities WHERE memberid = ?
       DELETE FROM tbl_member WHERE memberid = ?
       으로 실행된다는 것이다.
       즉, JPA가 직접 자식행 부터 삭제 한후 부모행을 삭제해 주는 것이다. 
       ALL 은 PERSIST, REMOVE, MERGE, REFRESH, DETACH 을 모두 포함한다.
    */
    
    /*
      orphanRemoval = true 
      의 뜻은 부모 컬렉션에서 제거된 자식은 DB에서도 삭제한다는 말이다.
      
      즉, member.getAuthorities().remove(auth); 를 하면 DB에서도 해당 auth 행이 삭제된다.
    */
    
    /*
      fetch = FetchType.LAZY
      이것은 조회 전략이다. 
      
      Member member = memberRepository.findById("eomjh"); 를 할때 
      이 시점에는 Member만 조회(select)해주고, Authority는 조회를 안 한다.
      Authority 는 member.getAuthorities(); 를 호출하는 순간 SELECT 쿼리 실행하여 조회를 해준다.
      
      만약에 
      fetch = FetchType.EAGER 로 하면 
      Member member = memberRepository.findById("eomjh"); 를 할때
      
      SELECT *
      FROM tbl_member m
      LEFT JOIN tbl_authorities a
      
      자동으로 OUTER JOIN 이 실행되어 Authority 까지 한꺼번에 모두 select 를 해온다.
      실무에서는 LAZY가 기본 권장사항 이다.
    */
    
 // >>> Entity 를 DTO 로 변환하기 <<<
 	public MemberDTO toDTO() {
 	    
 	    return MemberDTO.builder()
 	    		.memberid(this.memberid)
 	    		.passwd(this.passwd)
 	    		.enabled(this.enabled)
 	    		.name(this.name)
 	    		.registerday(this.registerday)
 	    		.lastLoginDate(this.lastLoginDate)
 	    		.authorities(this.getAuthorities()
 	    				         .stream()
 	    				         .map(Authorities::getAuthority)
 	    				         .toList())
 	    		.build();
 	}
    
}
