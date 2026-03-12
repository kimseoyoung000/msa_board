package com.spring.app.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// JwtToken 클래스는 일반적으로 Spring Security + JSON Web Token 기반 인증 구조에서 
// 클라이언트에게 발급해 줄 토큰(Response DTO) 역할을 하는 클래스이다. 
@Data
@AllArgsConstructor
@Builder
public class JwtToken {
	
	// 토큰에 필요한 4가지 필드
	
	private String grantType;  // 토큰 인증 방식(Authorization Scheme)
	/* grantType 은 HTTP 요청 시 Authorization 헤더에 다음과 같이 사용된다.
       
       Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
	   여기서 Bearer가 바로 grantType 이다.

	   ▣ Bearer 는 OAuth 2.0 스펙 기반 인증 구조에서 사용하는 토큰 타입 명시값 이다.
	     OAuth = Open Authorization → "개방형 권한 위임 표준"
	     
       예를 들어, 어떤 A라는 웹사이트에서
       "Google 계정으로 로그인" 버튼을 누르는 경우
       이때 사용자는 비밀번호를 A라는 웹사이트에 주지 않는다.
       대신 Google이 "이 사용자가 맞다"라고 보증해주고, 접근 권한을 Google이 토큰으로 전달해준다.

       이 구조가 바로 OAuth 2.0 기반이다.
       대표적인 제공자 예: Google, Facebook, Naver, Kakao	     
	*/
	
	private String accessToken;  // 실제 인증에 사용되는 JWT 문자열
	/*
	   ▣ 역할
         1. 사용자가 로그인하면 서버에서 발급해주는 것임
         2. 이후 모든 API 요청 시 헤더에 accessToken 을 포함시킴 
         3. 서버는 이 accessToken 을 검증하여 인증 처리함

       ▣ 포함 정보
         JWT 인 accessToken 에는 다음의 정보가 들어간다.
         1. 사용자 식별자 (username 또는 userId)
         2. 권한(role)
         3. 만료 시간(exp)
         4. 발급 시간(iat) 
	 */
	
	private Long accessTokenExpiresIn; 
	// accessToken 의 만료 시간 (Expiration Time)
	
	private String refreshToken;
	// JWT 인 accessToken 이 만료되었을 때 재발급을 위한 토큰
	/*
	   refreshToken은
       로그인이 성공되어지면 
       서버가 생성해서
       클라이언트에게 주고,
       동시에 서버 DB에도 저장하는 값이다.
       
       DB 저장은 "검증용" 이고 
       클라이언트 보관은 "재발급 요청용" 이다.
       
       클라이언트는 refreshToken 을 어디에 저장하나요?

       방식 ① JSON 응답으로 받는 경우
		{
		  "accessToken": "AAAAAA",
		  "refreshToken": "RRRRRR"
		}
		
		클라이언트는 이 값을:
		LocalStorage (권장 안함)
		SessionStorage (권장 안함)
		메모리 (SPA)
		쿠키
		중 하나에 저장한다.

        방식 ② (권장 방식) HttpOnly Cookie
        이 경우 클라이언트는 refreshToken 값을 직접 알 필요가 없다.

        서버가 응답할 때:
        Set-Cookie: refreshToken=RRRRRR; HttpOnly; Secure;
        브라우저가 refreshToken 값을 자동 저장한다.
        JS에서는 접근이 불가하다.
        accessToken 재발급 요청 시 브라우저가 refreshToken 값을 서버로 자동 전송해준다.
	*/
	
    /*
	   ▣  특징
	   -----------------------------------------------
	    구분	       Access Token	        Refresh Token
	              (accessToken)         (refreshToken)
	   -----------------------------------------------
	    사용 목적	   API 접근	            토큰 재발급
	    유효기간	   짧음 (예: 30분~1시간)	김 (예: 7일~30일)
	    저장 위치	   "클라이언트"	        "서버 또는 DB"
	
	   ▣ 동작 흐름
         1. Access Token 만료
	     2. 클라이언트 → Refresh Token 전송
	     3. 서버가 Refresh Token 검증
	     4. 새로운 Access Token 발급
	*/
}
