package com.spring.app.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.spring.app.auth.domain.CustomUserDetails;
import com.spring.app.member.domain.MemberDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/*
	Spring Security 와 JSON Web Token 을 사용하여
    인증(Authentication) 및 인가(Authorization)를 처리하기 위해
    JWT의 생성, 서명 검증, Claim 추출, Authentication 객체 변환 기능을 제공하는 클래스이다.
*/

@Component
@Slf4j
public class JwtTokenProvider {
	
	private static final String AUTHORITIES_KEY = "auth";
    
	private static final String BEARER_TYPE = "Bearer";
    // "Bearer" 는 OAuth 2.0 스펙 기반 인증 구조에서 사용하는 토큰 타입 명시값 이다.
 
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; 
    // 60분. Access Token (액세스 토큰) 유효기간
    
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  
    // 7일. Refresh Token (리프레시 토큰) 유효기간
    
    private final SecretKey secretKey;
    

    // 생성자
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) { 
    	                 // application.yml에서 secret 값 가져와서 secretKey 에 저장
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        // HMAC은 Hash-based Message Authentication Code 의 약자로서 
        // 비밀키를 사용해 데이터의 위변조 여부를 검증하는 해시 기반 인증 방식을 말한다. 
    }

    
    /* 인증된 사용자의 정보(Authentication authentication)를 가지고 
       AccessToken 과 RefreshToken 을 생성하여 JWT 토큰을 생성해주는 메서드 */
    public JwtToken generateToken(Authentication authentication) {
        
    	// >> 권한 가져오기 <<
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // >> Access Token (액세스 토큰) 유효기간. 현재로 부터 30분 <<
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME); 

        
        // >> Access Token 생성 << 
        // - 사용자가 인증된 상태임을 증명하는 주요 토큰으로서, 인증된 사용자의 권한 정보와 만료 시간을 담고 있음.
        // - API 요청 시, **헤더(Authorization: Bearer 토큰)**에 포함되어 서버에 인증 정보를 전달함.
        // - 만료 시간이 짧음 (예: 15분 ~ 1시간).
        //   짧은 만료 시간의 이유?
        //   -> 보안 강화를 위해 만료된 후 새 토큰을 받아야 함 (토큰 탈취 위험 감소).
        //   -> 일반적으로 Access Token 이 만료되면, Refresh Token 을 사용하여 Access Token 을 재발급 함.
        String accessToken = Jwts.builder()
                .subject(authentication.getName())    // 인증된 사용자의 아이디
                .claim(AUTHORITIES_KEY, authorities)  // 인증된 사용자의 권한(역할)정보를 Claim 속에 포함시킴
                .expiration(accessTokenExpiresIn)     // Access Token (액세스 토큰) 유효기간
                .signWith(secretKey)  // 서명(암호화)수행
                .compact();

        // >> Refresh Token 생성 <<
        // - Access Token 이 만료되었을 때, 새로운 Access Token 을 발급받기 위해 사용함.
        // - 만료 시간이 Access Token 보다 더 김(예: 7일 ~ 30일 이상).
        // - 보안 강화를 위해 Access Token 보다 더 안전한 저장소(DB 와 HttpOnly 쿠키)에 저장함.
        // - 클라이언트는 Refresh Token 을 JavaScript 로 제어가 불가하며, accessToken 재발급 요청 시 브라우저가 refreshToken 값을 서버로 자동 전송해준다.  
        //   Access Token 은 탈취되더라도 만료 시간이 짧아 피해를 최소화할 수 있음.
        //   Refresh Token 이 탈취되면 Access Token 을 무제한으로 재발급할 수 있기 때문에, 보안이 더 중요함.
        String refreshToken = Jwts.builder()
                .expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))  // Refresh Token (리프레시 토큰) 유효기간
                .signWith(secretKey)  // 서명(암호화)
                .compact();

        // >> "JWT 토큰 생성하기" <<
        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
        
        /*
            java.util.Date.getTime() 메소드
            - 현재 날짜 및 시간과 Epoch(UTC 기준 1970년 1월 1일 00:00:00 UTC) 간의 차이를 밀리초 단위로 반환해준다.
              타임존(Timezone)에 영향을 받지 않고, UTC 기준의 밀리초 값을 제공해준다.
              System.currentTimeMillis() 와 동일한 개념이지만, Date 객체에 종속된 방식이다. 
        */
    }

    
    
    /*
        >> 실제 인증에 사용되는 JWT 문자열인 accessToken 을 복호화(해독) 하여 
           accessToken에 들어있는 정보를 꺼내오는 메서드 <<
           
        - 주어진 Access token 을 가지고 JWT 토큰 복호화(해독) 하여, 
          인증된 사용자의 인증정보(Authentication)를 생성함.
          
        - 토큰의 Claims 에서 권한 정보를 추출하고, 
          User 객체를 생성한 후 Authentication(인증된 사용자의 인증정보) 객체로 반환함.
        
        SecurityContext 란?
        - Spring Security 에서 현재 인증된 사용자(Authentication)를 저장하는 컨테이너 역할을 하는 객체이다.
          즉, 로그인한 사용자의 보안 컨텍스트(Security Context)를 유지하는 공간으로서, 현재 로그인한 사용자 정보를 가져오는 데 사용된다.
          
        SecurityContext 는 Authentication 객체를 저장한다.
        
        SecurityContext   --> Authentication 객체를 저장하는 컨테이너
		└── Authentication (인증이 완료된 사용자 전체 컨텍스트) --> 현재 사용자의 인증 정보 객체 (UsernamePasswordAuthenticationToken 등)
    		├── Principal(UserDetails)  (사용자 정보)      --> 로그인한 사용자 정보
    		├── Credentials             (자격 증명, 예: 비밀번호)
    		├── Authorities             (사용자 권한 목록)  --> 사용자의 권한 목록 (예: ROLE_USER, ROLE_ADMIN)
        
    */
    public Authentication getAuthentication(String accessToken) {
        
    	// >> 아래에 parseClaims(String accessToken) 메서드를 만들어 두었음. <<
    	Claims claims = parseClaims(accessToken); 
    	// accessToken 을 가지고 JWT(JSON Web Token) 속에서 클레임(Claims)을 추출하기.
    	
        /*
        ======================================================
	        Claims(클레임)란? - Payload 에 포함된 데이터
			
			json
			
			{
			  "sub": "userId",
			  "role": "ROLE_USER",
			  "exp": 1699635999
			}
    	======================================================
        */
    	
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

     // 클레임에서 권한정보(역할) 가져오기
        Collection<? extends GrantedAuthority> authorities = 
        		Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                      .map(SimpleGrantedAuthority::new)
                      .collect(Collectors.toList());
        
        String memberid = claims.getSubject(); // claims.getSubject(); 가 아이디를 가져오는 것임. 

        // MemberDTO 생성 
        MemberDTO memberDto = new MemberDTO();
        memberDto.setMemberid(memberid);
        
        // 권한 세팅
        memberDto.setAuthorities(
                authorities.stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.toList())
        );

        // CustomUserDetails 생성 
        CustomUserDetails principal = new CustomUserDetails(memberDto);

        return new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );
        /* UsernamePasswordAuthenticationToken 은 Authentication 인터페이스를 구현한 클래스 중 하나로서,
           사용자의 인증 정보를 담아 SecurityContext 에 저장할 때 사용된다.
           첫번째 파라미터는 사용자정보, 두번째 파라미터는 비밀번호, 세번째 파라미터는 권한의 종류 이다. */
    }

    
    /*
       >> 토큰 정보를 검증하는 메서드 <<
          
       JWT(Json Web Token)의 유효성을 검증해서, 
       문제가 없으면 true를 반환하고, 문제가 있으면 false 를 반환한다.
    */
    public boolean validateToken(String token) {
    	
    	try {
            Jwts.parser()  // JWT를 파싱하는 객체 생성  
                .verifyWith(secretKey) // "이 키로 서명을 검증하겠다" 라는 검증에 사용할 키를 세팅하는 것  
                .build()
                .parseSignedClaims(token); // 이 secretKey로 서명 검증을 수행하고, 유효하면 Claims를 반환한다 
                                           /* 서명 검증은 보내온 token 속의 서명값과, token 속의 데이터를 가지고 secretKey 로 서명을 만들어서 
                                              이 2개의 서명이 서로 일치하면 유효한 토큰으로 판단하고 일치하지 않으면 유효하지 않은 토큰으로 판단한다. */
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token : ", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token : ", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token : ", e);
        } catch (IllegalArgumentException e) {
        	// 토큰이 올바른 형식이 아니거나 클레임이 비어있는 경우 등에 발생
            log.info("JWT claims string is empty : ", e);
        }
    	
        return false;
    }

    
    
   /*
     >> 실제 인증에 사용되는 JWT 문자열인 accessToken 에서 클레임(Claims)을 추출하는 메서드 <<  
     즉, JWT를 파싱하여 유효한 경우 클레임을 반환하고, 만료된 경우에도 클레임을 반환함.
    
     ======================================================
        Claims(클레임)란? - Payload 에 포함된 데이터(Claim)
		
		json
		
		{
		  "sub": "userId",
		  "role": "ROLE_USER",
		  "exp": 1699635999
		}
	 ======================================================	
   */
    private Claims parseClaims(String accessToken) { 
        	
        try {
            return Jwts.parser() // accessToken 을 해석(파싱)하는 객체 생성           
                    .verifyWith(secretKey) // 서명(Signature) 검증을 수행 (서명이 유효하지 않으면 예외 발생)
                    .build()
                    .parseSignedClaims(accessToken) // 토큰을 파싱하여 클레임을 추출
                    .getPayload(); // JWT의 본문(Claims) 반환
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
    
    
}
