package com.spring.app.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                 // lombok 에서 사용하는 @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 를 모두 합쳐놓은 것이다.
@AllArgsConstructor   // MemberDTO 의 모든 필드가 들어있는 파라미터가 있는 생성자를 만들겠다는 것이다.
@NoArgsConstructor    // MemberDTO 의 기본생성자를 만들겠다는 것이다.
@Builder              // 생성자 대신, 필요한 값만 선택해서 체이닝 방식으로 객체를 만들 수 있게 해주는 것.
public class MemberDTO {
 /* 먼저 오라클에서
    회원 테이블(인증 테이블)인 tbl_member 테이블과
    어쏘러티(권한, 역할) 테이블인 tbl_authorities 테이블을 만들어야 한다. */

	private String memberid;  // 회원아이디
	private String passwd;    // 비밀번호 (Spring Security 에서 제공해주는 SHA-256 암호화를 사용하는 대상) 
	private String enabled;   // !!! Spring Security 에서는 enabled 컬럼의 값이 1이어야만 회원이 존재하는것으로 인식한다. 반드시 enabled 컬럼이 존재해야만 한다.!!!  
 
	private String name;      // 회원명
	private LocalDate     registerday;     // 회원가입 일자 
	private LocalDateTime lastLoginDate;   // 최근에 마지막으로 로그인한 일자 및 시각
 
	private List<String> authorities;      // 권한
	
	
	// com.spring.app.member.service.MemberService_imple 의 @Override public List<MemberDTO> allMember() throws Exception 에서
	// MemberDTO 로 변환 때문에 필요함.
	public MemberDTO(String memberid, String name, LocalDate registerday, LocalDateTime lastLoginDate) {
		this.memberid = memberid;
		this.name = name;
		this.registerday = registerday;
		this.lastLoginDate = lastLoginDate;
	}
	
}
