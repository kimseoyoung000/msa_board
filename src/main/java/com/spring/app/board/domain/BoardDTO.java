package com.spring.app.board.domain;

import java.time.LocalDateTime;

import com.spring.app.member.domain.MemberDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                 // lombok 에서 사용하는 @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 를 모두 합쳐놓은 종합선물세트인 것이다.
@AllArgsConstructor   // 모든 필드 값을 파라미터로 받는 생성자를 만들어주는 것
@NoArgsConstructor    // 파라미터가 없는 기본생성자를 만들어주는 것
@Builder              // 생성자 대신, 필요한 값만 선택해서 체이닝 방식으로 객체를 만들 수 있게 해주는 것.
public class BoardDTO {

	private Long num;
	private String memberid;
	private String name;
	private String subject;
	private String content;
	private LocalDateTime regDate;
	private Integer readCount;
	
	private MemberDTO memberDto;
	
}

