package com.spring.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/*
 - 클라이언트가 접속하는 모든 페이지 마다 JWT 인증을 하기 위한 커스텀 필터로서, 
   UsernamePasswordAuthenticationFilter 이전에 실행 할 것이다.
   
 - 클라이언트에서 요청을 보내온 Http Request Header 에서 JWT 토큰을 추출하여, 
   유효한 토큰인 경우라면 해당 토큰에서 사용자 인증 정보(Authentication)를 가져와서,
   SecurityContext에 저장함으로 인증 요청을 마무리 하도록 한다.
*/
@RequiredArgsConstructor // @RequiredArgsConstructor는 Lombok에서 제공하는 애너테이션으로,
                         // final 필드 또는 @NonNull이 붙은 필드에 대한 생성자를 자동 생성해 준다. 

public class JwtAuthenticationFilter extends GenericFilterBean {
    
	public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";	

	private final JwtTokenProvider jwtTokenProvider;
	
	/*
	    @RequiredArgsConstructor 애너테이션으로 인해 Lombok이 자동으로 아래 생성자를 생성해준다.
	    
	    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		    this.jwtTokenProvider = jwtTokenProvider;
		}
		
		즉, JwtTokenProvider jwtTokenProvider 은 @Component 로 되어있어서 Bean 으로 설정되어 있으므로
		생성자 주입(DI)으로 되어지는 것이다.
	*/
    
	
	/*
	  - resolveToken() 메서드를 사용하여 Http Request Header(요청 헤더)에서 JWT 토큰을 추출한다. 
	  - JwtTokenProvider의 validateToken() 메서드로 JWT 토큰의 유효성을 검증한다. 
	  - 토큰이 유효하면 JwtTokenProvider의 getAuthentication() 메서드로 인증 객체를 가져와서 SecurityContext에 저장한다. 
	  - chain.doFilter()를 호출하여 다음 필터로 진행한다. 
	*/
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// 클라이언트에서 요청을 보내온 Http Request Header에서 JWT 토큰 추출
		String token = resolveToken((HttpServletRequest) request);  // resolveToken() 메서드는 아래에 정의해 두었음.
        
	//	System.out.println("~~~~~ 확인용 HttpServletRequest Header에서 추출한 JWT 토큰 : " + token);
        // ~~~~~ 확인용 HttpServletRequest Header에서 추출한 JWT 토큰 : null
        // ~~~~~ 확인용 HttpServletRequest Header에서 추출한 JWT 토큰 : eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzZW95aCIsImF1dGgiOiJST0xFX1VTRVIiLCJleHAiOjE3NDI3MjQ1OTV9.dKLmiahFM6Cse4xwiAThZZiUk1ejf1XHsQeFj7xvaqSGQkcR46etD003seGYnbcd
		
		if (token != null && jwtTokenProvider.validateToken(token)) {
			// HttpServletRequest Header에서 추출한 JWT 토큰이 null 이 아니라면, 즉 토큰을 가지고 Server API 에 접근을 한 경우라면  
	        // JwtTokenProvider 클래스에서 생성한 validateToken() 메서드를 사용하여 
			// 클라이언트가 보내온 토큰의 유효성 검사를 해서 토큰이 유효한 경우라면 
			
			Authentication authentication = jwtTokenProvider.getAuthentication(token);

System.out.println("@@@ 확인용 msa_board 의 doFilter 메서드 호출. 인증사용자 아이디 : " + authentication.getName());
			
			SecurityContextHolder.getContext().setAuthentication(authentication); 
			// 토큰을 가지고 JwtTokenProvider 클래스의 getAuthentication() 메서드를 실행하여 추출해온   
			// Authentication 객체 authentication(인증된 사용자의 인증정보) 를 SecurityContext에 저장한다.
			
			/*
				SecurityContext 란?
		        - Spring Security 에서 현재 인증된 사용자(Authentication)를 저장하는 컨테이너 역할을 하는 객체이다.
		          즉, 로그인한 사용자의 보안 컨텍스트(Security Context)를 유지하는 공간으로서, 현재 로그인한 사용자 정보를 가져오는 데 사용된다.
		          
		        SecurityContext 는 Authentication 객체를 저장한다.
		        
		        SecurityContext                         --> Authentication 객체를 저장하는 컨테이너
				└── Authentication  (인증 정보)           --> 현재 사용자의 인증 정보 객체 (UsernamePasswordAuthenticationToken 등)
		    		├── Principal   (사용자 정보)          --> 로그인한 사용자 정보 (보통 UserDetails 구현체)
		    		├── Credentials (자격 증명, 예: 비밀번호)
		    		├── Authorities (사용자 권한 목록)      --> 사용자의 권한 목록 (예: ROLE_USER, ROLE_ADMIN)
			*/    		
		}
		
		chain.doFilter(request, response); // 다음 필터로 요청을 전달
		
	}

	
	/*
	    Request Header 에서 토큰 정보 추출하기
	    - 주어진 HttpServletRequest 에서 토큰 정보를 추출하는 역할
	    - "Authorization" 헤더에서 "Bearer " 접두사로 시작하는 토큰을 추출하여 반환
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.split(" ")[1].trim();
		}
		
		return null;
	}
}
