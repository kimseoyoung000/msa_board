package com.spring.app.auth.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.spring.app.member.domain.MemberDTO;

import lombok.Getter;


@Getter
public class CustomUserDetails implements UserDetails {
	// Spring Security 에서 인증된 사용자의 정보는 UserDetails 인터페이스를 구현하여 관리한다.
    // UserDetails 인터페이스를 구현한 클래스는 로그인한 사용자의 정보 및 사용자의 권한(Role), 인증 토큰(JWT) 등을 포함할 수 있는 객체가 되어진다.
		
	private static final long serialVersionUID = 1L;
	
	private MemberDTO memberDto;
	
	public CustomUserDetails(MemberDTO memberDto) {
		this.memberDto = memberDto;
	}
	
	
	// 권한종류
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Collection 의 자식 인터페이스가 List , Set 등 이 있다. 그러므로 return 되어지는 타입은 List 가 되든 Set 이 되든 상관이 없는 것이다.
		// GrantedAuthority 는 "사용자에게 부여된 권한(Authority)" 을 표현하는 인터페이스 이며, "ROLE_USER", "ROLE_ADMIN" 같은 권한 문자열을 담는 역할을 한다. 
		
		// --- 첫번째 방법 : for문 사용하기 ---
	/*	
		List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
		
		List <String> authorityList = memberDto.getAuthorities();
		// memberDto.getAuthorities() 은 DB에서 조회한 권한 "문자열" 목록 (["ROLE_USER", "ROLE_ADMIN"] 등)
		
		for(String role : authorityList) {
			grantedAuthorityList.add(new SimpleGrantedAuthority(role));
			// SimpleGrantedAuthority 는 Spring Security 에서 제공하는 GrantedAuthority 구현체 클래스임.
		}// end of for------------------------------
		
		return grantedAuthorityList;
	*/	
		
		// --- 두번째 방법 : stream() 사용하기 ---
		return memberDto.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
		
		// memberDto.getAuthorities() 은 DB에서 조회한 권한 "문자열" 목록 (["ROLE_USER", "ROLE_ADMIN"] 등)
		// SimpleGrantedAuthority 는 Spring Security 에서 제공하는 GrantedAuthority 구현체 클래스임.

	}

	
	// 아이디
	@Override
	public String getUsername() {
		return memberDto.getMemberid();
	}
		
	
	// 비밀번호
	@Override
	public String getPassword() {
		return memberDto.getPasswd();
	}

	
	@Override
    public boolean isAccountNonExpired() {
        // 계정이 만료기간이 없는 무제한으로 사용가능한가?
		return true; // 필요 시 로직 추가
    }

    @Override
    public boolean isAccountNonLocked() {
    	// 계정을 잠금을 안한 상태인가?
    	return true; // 필요 시 로직 추가
    }

    @Override
    public boolean isCredentialsNonExpired() { 
    	// 자격 증명(비밀번호) 만료기간이 없는가?
        return true; // 필요 시 로직 추가
    }

    @Override
    public boolean isEnabled() {
        // 계정이 사용가능한가?
    	return true;
    }
    
    // *** 추가 정보 접근용 getter *** //
    // 회원명
    public String getMemberName() {  // public String getName() { 을 사용하면 안된다.
    	                             // 왜냐하면 Spring Security의 Authentication 객체 자체가 이미 getName()이라는 메서드를 기본적으로 가지고 있기 때문이다.
    	                             // 이 메서드는 사용자의 아이디(Username)를 반환하도록 설계되어 있다.
    	                             // 그러므로 사용자의 이름을 반환하기 위하여 만든 메서드 이라면 getName() 을 피해서 다른 이름인 getMemberName() 이라고 사용해야 한다.  
    	return memberDto.getName(); 
    }
    
    
    // 가입일자
    public LocalDate getRegisterday() {
    	return memberDto.getRegisterday();
    }
    
    
    // 최근에 마지막으로 로그인한 일자 및 시각(LocalDateTime 으로 사용하면 2026-03-01T11:09:15 와 같이 나오므로 T 를 제거하려고 리턴타입을 String 으로 함) 
    public String getLastLoginDate() {
    	return String.join(" ", String.valueOf(memberDto.getLastLoginDate()).split("T"));
    }
    
}
