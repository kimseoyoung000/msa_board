package com.spring.app.board.service;

import static com.spring.app.entity.QBoard.board;
import static com.spring.app.entity.QMember.member;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.app.board.domain.BoardDTO;
import com.spring.app.board.repository.BoardRepository;
import com.spring.app.entity.Board;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService_imple implements BoardService {

	// === Query DSL(Domain Specific Language) 를 사용하여 구하기 === //
	/*
	   QueryDSL은 Java 기반의 오픈 소스 프레임워크로, JPQL (Java Persistence Query Language)을 Java 코드로 작성할 수 있도록 해준다. 
	   즉, SQL이나 JPQL과 같은 데이터베이스 쿼리를 자바 코드로 작성하여, 타입 안정성과 코드 가독성을 높여준다.  
	*/

	/*
	   QueryDSL은 Java 기반의 오픈 소스 프레임워크로, JPQL (Java Persistence Query Language)을 Java 코드로 작성할 수 있도록 해준다. 
	   즉, SQL이나 JPQL과 같은 데이터베이스 쿼리를 자바 코드로 작성하여, 타입 안정성과 코드 가독성을 높여준다.  
	   
		1. 특정 조건으로 조회하기 (WHERE)
		
		같다:       .eq()
		크거나 같다: .goe()
		작거나 같다: .loe()
		크다:      .gt()
		작다:      .lt()
		포함한다 (LIKE %값%): .contains()
		시작한다 (LIKE 값%):  .startsWith()
		끝난다 (LIKE %값):   .endsWith()
		널(null)이다: .isNull()
		널(null)이 아니다: .isNotNull()
		목록 안에 있다 (IN): .in()
		
		
		2. 정렬하기 (ORDER BY)
		
		오름차순: .asc()
		내림차순: .desc()
		
		
		3. **일부만 가져오기 (LIMIT/OFFSET 또는 페이징)
		
		시작점 지정:     .offset()
		가져올 개수 지정: .limit()
		
		
		4. **그룹화 및 집계 함수 (GROUP BY, COUNT, SUM, AVG 등)
		
		그룹화:    .groupBy()
		개수 세기: .count()
		합계:     .sum()
		평균:     .avg()
		최대값:    .max()
		최소값:    .min()
		
		
		5. **조건이 여러 개일 때 (AND, OR)
		
		AND 조건: .and()
		OR 조건:  .or()
	*/	
	
	private final BoardRepository boardRepository;
	
	private final JPAQueryFactory jpaQueryFactory;
	
	
	@Override
	public Page<Board> getPageBoard(String searchType, String searchWord, int currentShowPageNo, int sizePerPage) {
		
	 //	Page<Board> page = null;
		Page<Board> page = Page.empty(); // 기본값으로 내용이 없는 빈 페이지임. null 아니므로 안전하게 메서드 호출 가능함.
		                                 // 검색 결과가 없을 때, 기본값으로 반환

	 
	/*	Spring Data JPA 에서 사용하는 Page 는 
	    페이징 처리된 데이터 및 
	    페이지 정보(총 페이지수, 전체 아이템 개수, 현재 페이지 번호, 현재 페이지의 아이템 등등)를 
	    제공해주는 인터페이스이다.

	    !!! Page 객체는 일반적으로 Repository의 조회 메서드(findAll(), findBy필드명, 등등)에서 Pageable 객체를 파라미터로 받아서 결과물을 반환할 때 사용된다. !!! 
	*/	   
		try {
			
			// 1. Pageable 객체를 생성하기
			
			Pageable pageable = PageRequest.of(currentShowPageNo-1, sizePerPage, Sort.by(Sort.Direction.DESC, "num")); 
		 /*
			   Spring Data JPA 에서 사용하는 Pageable 은 
			   "데이터를 페이지 단위로 검색"하고 제어하는 "인터페이스" 이다.
			   
			   Pageable 을 구현한 클래스인 PageRequest 의 static 메소드인 PageRequest.of(페이지번호, 한 페이지당 보여줄 행개수, 정렬기준); 메소드를 사용하여 객체를 생성하고,
			   이 객체를 Pageable 로 받아온다.
			   
			   생성된 Pageable pageable 객체에는 원하는 해당 페이지의 데이터들이 들어있다.   
	           그런데 '페이지 번호' 의 시작은 0 부터 시작한다.!!
	           
	           그리고 한 페이지당 보여줄 행개수(각 페이지별 보여질 목록의 개수)인 sizePerPage 만 알려주면 
			   Spring Data JPA(Java Persistence API)의 Pageable 은 
			   (OFFSET 행개수 ROW FETCH NEXT 행개수 ROW ONLY) 가 자동으로 적용되어 DB에서 데이터를 가져온다.
			   
	           짧게 말하면 "페이지번호, 한 페이지당 보여줄 행개수, 정렬기준" 설정을 통해 원하는 해당 페이지의 데이터를 가져온다.		    
		*/
			
			// 2. Repository의 조회 메소드에 파라미터로 Pageable 객체를 넣어서 페이징된 데이터를 가져오기
        
			//	>>> BooleanExpression은 QueryDSL 에서 제공해주는 클래스 이다.
			//	    BooleanExpression 클래스는 QueryDSL 전용의 SQL의 WHERE 조건 표현 객체로서 QueryDSL의 .where(), .and(), .or() 에만 사용된다. <<<
				
			BooleanExpression condition = Expressions.TRUE; 
				// Expressions.TRUE 라고 준것은 기본 조건 (항상 참)으로 시작해서 조건을 점진적으로 추가한다. 마치 WHERE 1=1 과 같은 뜻이다.  

			if ("subject".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "subject" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.subject.containsIgnoreCase(searchWord));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}

			if ("content".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "content" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.content.containsIgnoreCase(searchWord));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}    
			
			if ("all".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "all" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and( board.subject.containsIgnoreCase(searchWord).or(board.content.containsIgnoreCase(searchWord)) );
			 //	condition = condition.and(subjectCond.or(contentCond));
				// 괄호로 묶인 OR 조건을 먼저 만들고 AND로 연결
				// 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			} 
			
			if ("name".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "name" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(member.name.containsIgnoreCase(searchWord));
			    // 맨 위에서 import static com.spring.app.entity.QMember.member; 해야 함. 
			}
			
			List<Board> boards = jpaQueryFactory
					.selectFrom(board)                       // Board 엔티티를 기준으로 조회
					.join(board.member, member).fetchJoin()  // Board와 Member를 fetch join 으로 조인 (board.member 는 연관관계 필드). 페치 조인(Fetch Join)은 SQL의 표준 조인 종류가 아니라, JPA 에서 "성능 최적화" 를 위해 제공하는 기능으로서, SQL 한 번으로 연관된 엔티티나 컬렉션을 함께 모두 조회하여 영속성 컨텍스트에 올린다. 아래설명 참조.           
					.where(condition)                        // 조건절 (BooleanExpression 을 사용하여 동적 조건 처리)
					.offset(pageable.getOffset())            // 페이지 시작 위치 (예: 0, 10, 20...).  pageable.getOffset() 은 Spring Data JPA에서 페이징 처리 시 데이터베이스에서 조회를 시작할 위치를 말하는 것이다.       
					.limit(pageable.getPageSize())           // 한 페이지당 데이터 수
					.orderBy(board.num.desc())               // 정렬 기준 지정: num 컬럼 기준 내림차순 
					.fetch();                                // 최종적으로 리스트 형태로 결과 반환
			
			/*
			    pageable 은 위에서 만든 Pageable pageable = PageRequest.of(currentShowPageNo-1, sizePerPage, Sort.by(Sort.Direction.DESC, "num")); 이다. 
			    pageable.getOffset(); 의 계산방식  offset = (currentShowPageNo-1) * sizePerPage; 이다.
			    예를 들어, sizePerPage 가 10 일때
			    currentShowPageNo 가 1 이면 offset 은 0, 
			    currentShowPageNo 가 2 이면 offset 은 10, 
			    currentShowPageNo 가 3 이면 offset 은 20 이 된다. 
			 */

			
			// 특정 조건에 맞는 게시글의 총 개수를 조회
		    Long total = jpaQueryFactory
		    		.select(board.count())
		    	    .from(board)
		            .join(board.member, member)
		            .where(condition)
		            .fetchOne(); // 단일 결과를 반환한다. count 는 하나의 숫자만 반환되므로 fetchOne()을 사용한다.
		    /*
		      SQL 로 설명하면
		      
		      SELECT COUNT(*)
              FROM board JOIN member 
              ON board.fk_user_id = member.user_id
              WHERE [condition] 
              
              이다.
		     */
		    
		    /*
		       join(board.member, member) 와  join(board.member, member).fetchJoin() 의 차이점
		       
		       --------------------------------------------------------------------------------------
		        join(board.member, member)    VS   join(board.member, member).fetchJoin()
		       --------------------------------------------------------------------------------------
		        1. 주 엔티티만 영속화                        1. 주 엔티티 + 연관 엔티티 모두 영속화
		        2. LAZY(지연) 로딩 (추가 쿼리 발생)           2. EAGER(즉시) 로딩 (추가 쿼리 없음)
		        3. Board 는 영속성 컨텍스트에 저장됨           3. Board 와 Member 모두 영속성 컨텍스트에 저장됨
		           Member 는 프록시 객체로 남아 있음             Member 는 즉시 로딩됨 → 추가 쿼리 없이 바로 접근 가능   
		           board.getMember().getName() 호출 시 
		           추가 쿼리 발생
		 
		        **영속화(Persistence)**란? 엔티티 객체가 JPA의 영속성 컨텍스트에 등록되어 "관리되는 상태"를 말한다.
		        "영속성 컨텍스트" 는 DB와 애플리케이션 사이에서 객체의 상태를 추적하고 관리하는 논리적인 영역이다.
   	           
		        !!! 언제 fetchJoin() 을 사용해야 하는가? ==> 연관 엔티티의 데이터를 즉시 사용해야 할 때 !!!
		     */

		    page = new PageImpl<>(boards, pageable, total != null ? total : 0);
		    // Long total 값이 null 이 될 수 있으므로 total != null ? total : 0 으로 해주었다.
		    
		    /*
		        PageImpl 이란? 
		        Spring Data 에서 제공하는 "페이징 처리 결과" 를 담는 기본 구현 클래스이다.
                Page<T> 인터페이스를 구현하며, 페이징된 데이터와 관련 정보를 함께 제공해준다. 
                간단히 말하자면 페이징된 결과를 담기 위한 용도로 쓰이는 객체이다.
                
                생성자는 new PageImpl<>(contentList, pageable, totalCount); 이며
                첫번째 파라미터인 contentList 는 현재 페이지에 해당하는 데이터 목록이 들어오며,
                두번째 파라미터인 pageable 에는 요청된 페이지 정보(page, size, sort 등)가 들어오며,
                세번째 파라미터인 totalCount 에는 조건에 따른 전체 데이터 개수가 들어온다. 
		     */

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return page;
	}
		

	@Override
	public void insertBoard(Board entity) throws Exception {
		
		boardRepository.save(entity);
	}

	
	
	@Override
	@Transactional(readOnly = true)
	/*
	    @Transactional 이 없으면 Repository 호출 시점에만 트랜잭션이 잠깐 열림.
        Spring Data JPA 내부에서 findById() 실행 시점에만 트랜잭션이 열렸다가 바로 닫혀버림.
        이후 부터는 트랜잭션이 없으므로 Member는 JPA 영속 엔티티 인데, 
        트랜잭션이 없는 시점에서는 영속성 컨텍스트가 닫혀 버리므로 LAZY 연관 객체 접근이 불가하게 된다.
        그래서 board.getMember().getMemberid() 와 board.getMember().getName() 에서 오류가 발생하게 된다.
        
        그러므로 LAZY 연관 객체 접근까지 가능하도록 즉, 메서드 전체 끝까지 모두 다 수행한 후 트랜잭션을 닫히게 하려면 
        반드시 @Transactional(readOnly = true) 을 해주어야만 한다.!!!
         
        그래서 일반적으로 SELECT 를 하는 메서드에서는 @Transactional(readOnly = true) 을 넣어준다. 
	*/
	public BoardDTO viewBoard(long num) throws Exception {

		Board board = boardRepository.findById(num)
				.orElseThrow(() -> 
	               new IllegalArgumentException("존재하지 않는 게시글입니다. num=" + num));
		            // IllegalArgumentException 은 메서드에 전달된 인자가 잘못되었을 때 발생시키는 예외절
		            // 잘못된 값이 들어왔을 때. 즉, "전달된 매개변수(argument)가 유효하지 않다" 는 의미의 예외절
		
		return BoardDTO.builder()
				.num(board.getNum())
				.memberid(board.getMember().getMemberid())
				.name(board.getMember().getName())
				.subject(board.getSubject())
				.content(board.getContent())
				.regDate(board.getRegDate())
				.readCount(board.getReadCount())
				.build();
	}
	

	@Override
	public void updateBoard(Board entity) throws Exception {
		boardRepository.save(entity); // save : 행이 존재하면 update, 행이 없으면 insert 해줌
	}

	@Override
	public void deleteBoard(long num) throws Exception {
		boardRepository.deleteById(num);
	}

	
	
	@Override
	@Transactional 
	// @Transactional // JPA 에서는 DML 작업시 필수임. 성공시 commit 해주고, 실패시 roolback 해줌. 
    // JPA 에서는 오로지 1개의 DML 작업이 있는 경우에도 반드시 @Transactional 을 적어주어아 한다.!!                
    // 만약에 @Transactional 을 사용하지 않으면 SQL명령을 수행해도 DB에는 반영이 안되어진다. 
    // 그리고 jakarta.persistence.TransactionRequiredException: Executing an update/delete query 오류가 발생함.
	public void updateReadCount(long num) throws Exception {
	//	boardRepository.updateReadCount(num);
		
        long affectedRows = jpaQueryFactory
                .update(board)
                .set(board.readCount, board.readCount.add(1))
                .where(board.num.eq(num))
                .execute();

        System.out.println("~~~ affectedRows : " + (int) affectedRows);
        // ~~~ affectedRows : 1
	}


	// 이전 글(미래)
	@Override
	public BoardDTO getPreBoardDto(long num, String searchType, String searchWord) {

		Board entity = null;
		
		try {
			BooleanExpression condition = Expressions.TRUE; 
			// Expressions.TRUE 라고 준것은 기본 조건 (항상 참)으로 시작해서 조건을 점진적으로 추가한다. 마치 WHERE 1=1 과 같은 뜻이다.  

			if (searchType.isBlank() || searchWord.isBlank()) { // 검색이 없는 경우
			        
				condition = condition.and(board.num.gt(num));  // WHERE num > :num
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}

			else if ("subject".equals(searchType) && 
				     (searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "subject" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.subject.containsIgnoreCase(searchWord)).and(board.num.gt(num));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}

			else if ("content".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "content" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.content.containsIgnoreCase(searchWord)).and(board.num.gt(num));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}    
			
			else if ("all".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "all" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and( board.subject.containsIgnoreCase(searchWord).or(board.content.containsIgnoreCase(searchWord)) ).and(board.num.gt(num)); 
			 //	condition = condition.and(subjectCond.or(contentCond));
				// 괄호로 묶인 OR 조건을 먼저 만들고 AND로 연결
				// 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			} 
			
			else if ("name".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "name" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(member.name.containsIgnoreCase(searchWord)).and(board.num.gt(num));
			    // 맨 위에서 import static com.spring.app.entity.QMember.member; 해야 함. 
			}
		
			entity = jpaQueryFactory
		            .selectFrom(board)
		            .join(board.member, member).fetchJoin()  // Board와 Member를 fetch join 으로 조인 (board.member 는 연관관계 필드)
		            .where(condition)          // WHERE num > :num  AND ....
		            .orderBy(board.num.asc())  // ORDER BY num ASC
		            .limit(1)                  // FETCH FIRST 1 ROWS ONLY
		            .fetchOne();               // 1개 결과 반환 (null 이 나올 수 있음)
			// 또는 QueryDSL 5 이상에서는 
			// .limit(1)
			// .fetchOne(); 
			// 대신에 
			// .fetchFirst(); 도 가능함.
			
			// build.gradle 파일에 가보면 implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta' 라고 했으므로 우리는 가능하다.

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (entity != null) 
			return BoardDTO.builder()
					.num(entity.getNum())
					.memberid(entity.getMember().getMemberid())
					.name(entity.getSubject())
					.subject(entity.getSubject())
					.content(entity.getContent())
					.regDate(entity.getRegDate())
					.readCount(entity.getReadCount())
					.build();
		
		else 
			return null;
		
	}

	
    // 다음 글(과거)
	@Override
	public BoardDTO getNextBoardDto(long num, String searchType, String searchWord) {

		Board entity = null;
		
		try {
			BooleanExpression condition = Expressions.TRUE; 
			// Expressions.TRUE 라고 준것은 기본 조건 (항상 참)으로 시작해서 조건을 점진적으로 추가한다. 마치 WHERE 1=1 과 같은 뜻이다.  

			if (searchType.isBlank() || searchWord.isBlank()) { // 검색이 없는 경우
			        
				condition = condition.and(board.num.lt(num));   // WHERE num < :num
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}

			else if ("subject".equals(searchType) && 
				     (searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "subject" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.subject.containsIgnoreCase(searchWord)).and(board.num.lt(num));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}

			else if ("content".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "content" 가 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(board.content.containsIgnoreCase(searchWord)).and(board.num.lt(num));
			    // 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			}    
			
			else if ("all".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "all" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and( board.subject.containsIgnoreCase(searchWord).or(board.content.containsIgnoreCase(searchWord)) ).and(board.num.lt(num)); 
			 //	condition = condition.and(subjectCond.or(contentCond));
				// 괄호로 묶인 OR 조건을 먼저 만들고 AND로 연결
				// 맨 위에서 import static com.spring.app.entity.QBoard.board; 해야 함. 
			} 
			
			else if ("name".equals(searchType) && 
				(searchWord != null && !searchWord.trim().isEmpty()) ) { // 검색대상이 "name" 이 아니거나 검색어가 없거나 공백이라면 해당 조건은 무시됨.
			        
				condition = condition.and(member.name.containsIgnoreCase(searchWord)).and(board.num.lt(num));
			    // 맨 위에서 import static com.spring.app.entity.QMember.member; 해야 함. 
			}
		
			entity = jpaQueryFactory
		            .selectFrom(board)
		            .join(board.member, member).fetchJoin()  // Board와 Member를 fetch join 으로 조인 (board.member 는 연관관계 필드) 
		            .where(condition)           // WHERE num < :num  AND ...
		            .orderBy(board.num.desc())  // ORDER BY num DESC
		            .fetchFirst();              // FETCH FIRST 1 ROWS ONLY
		                                        // 1개 결과 반환 (null 이 나올 수 있음)
			// 또는 QueryDSL 5 이상에서는 
			// .limit(1)
			// .fetchOne(); 
			// 대신에 
			// .fetchFirst(); 도 가능함.
			
			// build.gradle 파일에 가보면 implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta' 라고 했으므로 우리는 가능하다.

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (entity != null)
			return BoardDTO.builder()
					.num(entity.getNum())
					.memberid(entity.getMember().getMemberid())
					.name(entity.getSubject())
					.subject(entity.getSubject())
					.content(entity.getContent())
					.regDate(entity.getRegDate())
					.readCount(entity.getReadCount())
					.build();
		else 
			return null;
	}
	
}
