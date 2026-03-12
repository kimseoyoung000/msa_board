package com.spring.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_board")
@Data                 // lombok 에서 사용하는 @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 를 모두 합쳐놓은 종합선물세트인 것이다.
@AllArgsConstructor   // 모든 필드 값을 파라미터로 받는 생성자를 만들어주는 것
@NoArgsConstructor    // 파라미터가 없는 기본생성자를 만들어주는 것
@Builder
public class Board {

	@Id
	@Column(name="num", columnDefinition="NUMBER")  // columnDefinition 은 DB 컬럼의 정보를 직접 주는 것이다. 예를 들어 columnDefinition = "Nvarchar2(20) default '사원'" 인 것이다. 
	@SequenceGenerator(name="SEQ_BOARD_GENERATOR", sequenceName="seq_board", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_BOARD_GENERATOR")
	private Long num;
	

 // 연관관계 정의
 //	@ManyToOne(fetch = FetchType.EAGER) // ManyToOne 에서는 fetch 전략을 작성하지 않아도, 기본값은 즉시 로딩인 EAGER 이다. 즉시 로딩이라서 Board 엔티티를 조회할 때 연관 엔티티인 Member 엔티티도 즉시 함께 조회되어짐. 성능 저하 가능성이 있음.
 //	@ManyToOne(fetch = FetchType.LAZY)  // fetch 전략을 지연 로딩인 LAZY 로 변경함으로 인해, Board 엔티티를 조회할 때 연관 엔티티인 Member 엔티티는 조회되지 않고, 연관 엔티티는 필요할 때(연관 엔티티 객체 메소드에 접근할 때) 조회된다. 성능 유리함. 
			
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberid", nullable = false, updatable=false)  // 이 필드는 UPDATE 를 할 수 없도록 제외시킴. 즉, 한번 데이터 입력 후 memberid 컬럼의 값은 수정 불가라는 뜻이다.   
	private Member member;
	// tbl_board.memberid 컬럼을 
	// JPA가 Member 엔티티 객체와의 연관관계로 직접 관리해준다.
	// tbl_board.memberid 컬럼에 입력되거나 수정되어지는 FK 값은 JPA가 알아서 자동으로 관리해준다.
		
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
	
	@Column(nullable=false, length=500)
	private String subject;
	
	@Column(nullable=false, length=4000)
	private String content;
	
 	@Column(columnDefinition="DATE DEFAULT SYSDATE",  
 			nullable = false, insertable=false, updatable=false)  // 이 필드는 columnDefinition = "DATE DEFAULT SYSDATE" 로 되어 있어서 INSERT 시 제외시켜도 괜찮음. 또한 UPDATE 도 할 수 없도록 제외시킴. 즉, 한번 데이터 입력 후 reg_date 컬럼의 값은 수정 불가라는 뜻이다.
	private LocalDateTime regDate;
	
	@Column(columnDefinition = "NUMBER DEFAULT 0", 
			nullable = false, insertable=false, updatable=false) // 이 필드는 columnDefinition = "NUMBER DEFAULT 0" 으로 되어 있어서 INSERT 시 제외시켜도 괜찮음. 또한 UPDATE 도 할 수 없도록 제외시킴.    
	private Integer readCount;  
	
		
	@PrePersist // INSERT 전에 호출 되어지는 것이다. 
	public void prePersist() {
		this.regDate = this.regDate == null 
				? LocalDateTime.now() 
				: this.regDate;
	}
	
}
