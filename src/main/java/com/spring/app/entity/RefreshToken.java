package com.spring.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_refreshtoken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    
	@Id
    @Column(name="memberid", nullable=false, length=50)
    private String memberid;
	
	@Column(name="rt_value", nullable=false, length=300)
    private String rtValue;

 // 연관관계 정의
 //	@ManyToOne(fetch = FetchType.EAGER) // ManyToOne 에서는 fetch 전략을 작성하지 않아도, 기본값은 즉시 로딩인 EAGER 이다. 즉시 로딩이라서 RefreshToken 엔티티를 조회할 때 연관 엔티티인 Member 엔티티도 즉시 함께 조회되어짐. 성능 저하 가능성이 있음.
 	@ManyToOne(fetch = FetchType.LAZY)  // fetch 전략을 지연 로딩인 LAZY 로 변경함으로 인해, RefreshToken 엔티티를 조회할 때 연관 엔티티인 Member 엔티티는 조회되지 않고, 연관 엔티티는 필요할 때(연관 엔티티 객체 메소드에 접근할 때) 조회된다. 성능 유리함. 
		     // name="외래키컬럼명", referencedColumnName="참조당하는컬럼명" 이다. // <!!중요!!> insertable=false, updatable=false 으로 설정해야만 한다. !!! 중요 !!!   
	@JoinColumn(name="memberid", referencedColumnName="memberid", insertable=false, updatable=false) // RefreshToken 엔티티의 memberid 컬럼을 Member 엔티티의 memberid 컬럼을 참조하는 foreign key 키로 설정하는 것임.      
	private Member member;
    // tbl_refreshtoken.memberid 컬럼을 
    // JPA가 Member 엔티티 객체와의 연관관계로 직접 관리해준다.
    // tbl_authorities.memberid 컬럼에 입력되거나 수정되어지는 FK 값은 JPA가 알아서 자동으로 관리해준다. 
 
 	public RefreshToken updateValue(String token) {
        this.rtValue = token;
        return this;
    }
 	
	/*
	  
	  >> EAGER 예제 <<
	  List<Board> boards = boardRepository.findByFkUserId("leess").get();
	  
	  실행되는 SQL (EAGER일 때)
	  -- 1. Board 조회
      select * from tbl_board where fk_user_id = 'leess';

      -- 2. 연관된 Member 즉시 조회
      select * from tbl_member where user_id = 'leess';
	  
	  /////////////////////////////////////////////////////////////////////
	  
	  >> LAZY 예제 <<
	  List<Board> boards = boardRepository.findByFkUserId("leess").get();
	  
	  // 아직 회원 조회 안 함
	  
	  Member member = boards.get(0).getMember();  // 여기서 접근
      
	  
	  실행되는 SQL (LAZY일 때)
	  -- 1. Board 조회
      select * from tbl_board where fk_user_id = 'leess';

      -- 2. getMember() 호출하는 순간 연관된 Member 조회
      select * from tbl_member where user_id = 'leess';
	  
	  //////////////////////////////////////////////////////////////////////
	  
	  실무에서는 대부분의 연관관계에 LAZY 를 기본으로 설정해서 사용함.
	*/
	
}
