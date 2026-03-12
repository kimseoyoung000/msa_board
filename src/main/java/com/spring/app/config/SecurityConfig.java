package com.spring.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.spring.app.security.JwtAuthenticationFilter;
import com.spring.app.security.JwtTokenProvider;

// import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Configuration        // Spring 컨테이너가 처리해주는 클래스로서, 클래스내에 하나 이상의 @Bean 메서드를 선언만 해주면 런타임시 해당 빈에 대해 정의되어진 대로 요청을 처리해준다. 

@EnableWebSecurity    // SecurityConfig 클래스로 시큐리티를 제어하고 싶다면 @EnableWebSecurity 어노테이션을 해주어야 한다. 
                      // 만약에 @EnableWebSecurity을 어노테이션을 붙이지 않으면 인덱스 홈페이지가 "/login"으로 리다이렉션된다. 

@EnableMethodSecurity // Controller 및 Service 상에서 생성한 메서드에 @PreAuthorize 를 사용하기 위한 전제조건인 것이다.
                      // @PreAuthorize 는 "메서드를 실행하기 전에, Spring Security에게 권한을 검사해달라" 는 뜻으로서, 메서드를 실행하기에 앞서서 "이 사람 권한 있어?" 하고 체크하고, 권한이 없으면 바로 막아버리는 것이다.
                      /*
                          @PreAuthorize("hasRole('ROLE_ADMIN')")
                          @GetMapping("/admin")
                          public String adminPage() {
                              return "admin/adminPage";
                          } 
                          1. 사용자가 /admin URL을 호출함
                          2. 메서드 실행 전에 Spring Security가 먼저 체크함
                          3. ROLE_ADMIN 권한이 있으면 → 메서드 실행
                          4. ROLE_ADMIN이 없으면 → 403 Forbidden(접근 거부)
                          즉, @PreAuthorize 은 서버에서 메서드 단위로 문지기 역할을 하는 어노테이션이다.
                             
                       ▶ 여러 권한 허용        => @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
                       ▶ 로그인 된 사용자만 허용 => @PreAuthorize("isAuthenticated()")
                      */

@RequiredArgsConstructor // @RequiredArgsConstructor는 Lombok 라이브러리에서 제공하는 애너테이션으로, 
                         // final 필드 또는 @NonNull이 붙은 필드에 대해 생성자를 자동으로 생성해준다.
public class SecurityConfig { 

	private final JwtTokenProvider jwtTokenProvider; // 생성자 DI(의존객체 주입)
		
	/*
	  비밀번호 암호화를 위해서 Bcrypt 라는 알고리즘을 사용한다. 
	  Bcrypt 알고리즘은 내부에서 랜덤으로 salt(암호화키)를 생성하여 이를 평문에 붙이고 여러번 해싱을 해주는 기능으로, spring-security에서 제공해주고 있다. 
	  비밀번호 암호화를 위해 BCryptPasswordEncodr의 객체를 이용하여 비밀번호를 암호화하여 DB에 저장하도록 한다.
	  이를 위해 BCryptPasswordEncoder 를 빈으로 등록하여 사용한다.
	*/
	@Bean
	public PasswordEncoder passwordEncoder(){
	
	    return new BCryptPasswordEncoder();
	}
	
	
	// == 인증 실패시 401 에러 관련 예외처리(유효한 자격증명을 제공하지 않고 접근하려 할때 발생하는 에러이며 에러번호가 401임) == //  
	@Bean
	AuthenticationEntryPoint customAuthenticationEntryPoint() {
	    return (request, response, authenticationEntryPointException) -> {
	        
	    	// == MVC 전통 방식 : 브라우저에서 요청하면 페이지이동을 서버가 결정하는 방식이라서 서버가 redirect 해줌 == 
	    //	response.sendRedirect(request.getContextPath()+"/exception/noAuthentication");
	        // 유효한 자격증명을 제공하지 않고 접근하려 할때 이동할 주소 지정 
	    	
	    	// == AJAX 방식 : AJAX 에서 요청하면 상태코드를 반환받아 JavaScript 에서 페이지이동을 결정하는 방식이라서 
	    	//    서버에서 redirect 해주면 안되고 상태코드만 내려주어야 함 == 
	    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    // 유효한 자격증명을 제공하지 않고 접근하려 할때 상태코드 401 만 내려주고, JavaScript 에서 내려받은 상태코드값에 따라 페이지이동을 결정해야 한다.   
	    	
	    };
	}
	
	// == 권한 실패시 403 에러 관련 예외처리(접근권한이 필요한 페이지에 접근권한이 없는 유저가 접속할 경우에 발생하는 에러이며 에러번호가 403임) == //  
	@Bean
	AccessDeniedHandler customAccessDeniedHandler() {
	    return (request, response, accessDeniedException) -> {
	       
	    	// == MVC 전통 방식 : 브라우저에서 요청하면 페이지이동을 서버가 결정하는 방식이라서 서버가 redirect 해줌 == 
	    //	response.sendRedirect(request.getContextPath()+"/exception/accessDeny");
	        // 접근권한이 필요한 페이지에 접근권한이 없는 유저가 접속할 경우 이동할 주소 지정
	    	
	    	// == AJAX 방식 : AJAX 에서 요청하면 상태코드를 반환받아 JavaScript 에서 페이지이동을 결정하는 방식이라서 
	    	//    서버에서 redirect 해주면 안되고 상태코드만 내려주어야 함 == 
	    	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		    // 유효한 자격증명을 제공하지 않고 접근하려 할때 상태코드 403 만 내려준다.
	    	// 유효한 자격증명을 제공하지 않고 접근하려 할때 상태코드 401 만 내려주고, JavaScript 에서 내려받은 상태코드값에 따라 페이지이동을 결정해야 한다. 
	    };
	}

	
	// SecurityFilterChain은 Spring Security에서 HTTP 요청을 처리하는 보안 필터들의 체인(순서 있는 목록)을 정의하는 인터페이스이다. 
	// 간단히 말해, 어떤 요청이 들어왔을 때 어떤 보안 필터들이 적용될지를 결정하는 역할을 하는 것이다.
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
	
	/*
	   CORS란?
       - HTTP 요청은 기본적으로 Cross-Site HTTP Requests 가 사용가능하다. 
         간단히 말하면, 다른 도메인의 Resource를 사용하는것이 가능하다는 말이다. 
         하지만 Cross-Site HTTP Requests는 Same Origin Policy를 적용 받기 때문에 요청이 불가하다. 
         즉 프로토콜, 호스트명, 포트가 같아야만 요청이 가능하다.
 
		 >> same-origin <<	
		 http://domainA/page1.html  --->  http://domainA/page2.html 
		  
		 >> cross-origin <<	
		 http://domainA/page1.html  --->  http://domainB/page.html	
    */

		
	/*
		==== HttpSecurity 클래스(스프링시큐리티의 거의 대부분설정을 담당하는 클래스)의 메서드 설명 ====
		------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------    
		     메서드	                                    설명
		------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
		 authorizeHttpRequests()	                권한설정하기 위한 기능을 가진 상위 메서드 
		    
		 requestMatchers()	                        특정 리소스에 대한 권한 설정하기 위해 사용
		 anyRequest()	                            requestMatchers() 로 지정된 리소스를 제외한 모든 리소스들을 묶어서 표현
		 authenticated()                            인증을 완료해야 접근이 가능함 
		 hasRole("롤명")	                            antMatchers 뒤에 붙으며, 권한의 이름을 적어 그 권한을 가진 사용자만 접근을 허용
		 hasAnyRole("롤명1","롤명2","롤명3",롤명여러개)   antMatchers 뒤에 붙으며, 나열된 권한의 이름을 적어 나열된 권한 중 하나라도 가진 사용자만 접근을 허용
		 permitAll()	                            인증절차없이 허용해줌을 의미
		 formLogin()	                            사용자 정의 로그인 기능과 로그아웃 기능을 가진 상위 메서드
		 loginPage()	                            사용자가 따로 만든 로그인 페이지를 사용하려고 할때 설정한다. 만약에 따로 설정하지 않으면 디폴트 URL이 "/login" 이기 때문에 "/login" 으로 호출하면 스프링이 제공하는 기본 로그인페이지가 호출된다.  
		 loginProcessingUrl()	                    해당 주소에서 로그인 프로세스를 진행해줌.
		                                            로그인 form 에서 아이디와 비번을 입력하고 확인을 클릭하면 "/auth/login" 를 호출 하게 되었들 때 인증처리하는 필터가 호출되어 아이디와 비번을 받아와 인증처리가 수행된다. 즉 인증을 처리하는 기본필터 UsernamePasswordAuthenticationFilter 가 실행 되는 것이다.   
		 defaultSuccessUrl()	                    인증이 성공했을 때, 리다이렉션할 페이지(이동하는 페이지)를 설정한다. 만약에 설정하지 않는경우 디폴트값은 "/" 이다. 
		 successHandler()                           인증이 성공했을 때, 그 이후 별도의 처리가 필요한 경우 커스텀 핸들러를 생성하여 등록할 수 있다. 
		                                            커스텀 핸들러를 생성하여 등록하면 인증성공 후 사용자가 추가한 로직을 수행하고 성공 페이지("/main")로 이동한다.  
		                                                
		 failureUrl()                                인증이 실패 했을 경우 이동하는 페이지를 설정한다
		 failureHandler()                            인증 실패 후, 별도의 처리가 필요한경우 커스텀 핸들러를 생성하여 등록할 수 있다.
		                                                커스텀 핸들러를 생성하여 등록하면 인증실패 후 사용자가 추가한 로직을 수행하고 실패 페이지("/login-fail")로 이동한다.
	*/		                                                
	 
     //	아무 설정도 하지 않으면 기본적으로 CSRF(데이터 위변조 방지)는 활성화된다. POST 방식에서는 서버에서 생성해준 csrf 토큰값을 다시 보내주어야만 성공되어진다.
		
	 // 아래는 CSRF 는 위변조 방지에 대한 사용유무를 체크하는 것으로 사용하지 않으려면 아래와 같이 하면 된다.
	 /*
		httpSecurity
    		.csrf((csrfConfig) -> csrfConfig
    			.disable()
    		);
     */		
		// Spring security 를 추가하면 기본적으로 CSRF 가 사용하는 것으로 설정되기 때문에, 
		// 모든 POST 방식의 데이터 전송에 있어서 토큰 값이 있어야 한다. 
		// 만약에 토큰 값이 없는 경우라면 POST 방식이 정상적으로 수행되지 않는다. 그래서 우리는 사용하지 않도록 한다. 
		/*
           CSRF 는 위변조 방지에 대한 사용유무를 체크하는 것으로 여기서 우리는 사용하지 않도록 설정 하겠다.
		       
           CSRF (Cross-Site Request Forgery) 란? 
           사이트 간 요청 위조(또는 크로스 사이트 요청 위조, 영어: Cross-Site Request Forgery, CSRF, XSRF)는 
           웹사이트 취약점 공격의 하나로, 사용자가 자신의 의지와는 무관하게 공격자가 의도한 행위(수정, 삭제, 등록 등)를 특정 웹사이트에 요청하게 하는 공격을 말한다.
           즉, 자신도 모르게 해킹공격코드가 설치된 사용자는 원본사이트에 로그인하여 글쓰기를 할 때, 해킹코드로 인하여 원본사이트와 동일하게 만든 피싱사이트(URL 주소가 거의 비슷하고, 내용물은 동일한 사이트)에 접속하게 되어  
           글내용을 전송할 경우 실제 전송되는 내용은 hidden 타입으로 숨겨진 데이터들이 피싱사이트로 전송되도록 하는 공격을 말한다.
           유명 경매 사이트인 옥션에서 발생한 개인정보 유출 사건이 바로 이러한 피싱사이트를 통해서 개인정보를 유출해간 것이다.
               
           Spring Security에서는 기본적으로 CSRF 공격을 방어하는데,
           GET 방식은 허용해주고 POST 방식에 대해서는 방어해서 막아버린다. 
               
           - CSRF 가 사용하는 것으로 설정된 상태에서 CSRF 토큰을 설정해주지 않으면 
             JSP 에서 보내는 POST 요청을 모두 막기 때문에 로그인, 회원가입 기능이 작동하지 않아 인증 과정을 진행할 수 없게 된다.
           - 이를 위해 JSP에서 POST 요청을 보낼 때 CSRF 토큰에 값을 넣어 함께 보내고, 
             Spring Security가 토큰 값을 확인하여 자신이 내려준 값이 맞는지 확인하는 방식으로 CSRF 공격을 판단한다.  

           아래가 CSRF Token 에 값을 넣어주는 코드이다.  
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />  
            - POST 요청을 보내도 Token에 담긴 값으로 CSRF 공격이 아니라고 판단하여 POST 요청을 막지 않고 허용해준다.  
               
           또는 번거롭게 <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> 을 넣을 필요없이 
           대신에 form 태그속에 security 태그인 <s:csrfInput/> 을 넣어주면 된다.  
       
           이와같이 CSRF 토큰 필터는 GET 방식의 데이터 전송에는 관여하지 않고, 
           오로지 POST 방식의 데이터 전송에만 관여한다.
           GET 방식의 데이터 전송에 대해서 CSRF 토큰 필터를 사용하지 않는 이유는 
           만약에 GET방식까지 막아버리면 다른 사이트에서 링크를 타고 들어오는 요청이나, 
           RESTful API 등을 처리할 수가 없게 되기 때문이다.         	        
       
           참조사이트 : https://velog.io/@wooryung/Spring-Boot-Spring-Security%EB%A5%BC-%EC%A0%81%EC%9A%A9%ED%95%98%EC%97%AC-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0
                     https://codevang.tistory.com/282
	    */
		
		String[] excludeUri = {"/",
				               "/board/list",
				               "/board/view",
				               "/favicon.ico"
				               }; 
		
		httpSecurity
	        .authorizeHttpRequests((auth) -> auth
                    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                     // DispatcherType 을 import 시 jakarta.servlet.DispatcherType 으로 함. 
                     // DispatcherType.FORWARD 은 Spring MVC에서 뷰 템플릿(JSP, Thymeleaf 등)으로 포워딩하는 경우 허용하는 것임.
                     // DispatcherType.ERROR 은 Spring Security 6부터는 에러 페이지로 리다이렉트되는 내부 요청(ERROR 타입)에 대해서도 보안 필터가 기본 적용될 수 있으므로 내부 요청(ERROR 타입)에 대해서는 모두 허용을 하는 것이다. 
                                          
                    .requestMatchers(excludeUri).permitAll()
                    
        //          .requestMatchers("/bootstrap-4.6.2-dist/**", 
        //            		         "/css/**", 
        //            		         "/images/**", 
        //            		         "/js/**").permitAll() // 정적 리소스 허용하기.  // "/resources/**" 은  /src/main/webapp/resources 를 가리키는 것이다. 
        //           permitAll() 은 "필터는 타지만 인증 검사만 안 한다"는 의미이고, ignoring() 은 "아예 필터를 타지 않는다"는 의미이다. 
        //           그래서 정적 리소스 허용은 permitAll() 도 가능하지만 우리는 아래에 ignoring() 을 사용하였다.             
                    
                    .anyRequest().hasAnyRole("USER","ADMIN") // 자동적으로 ROLE_ 붙여줌. 즉, ROLE_USER 와 ROLE_ADMIN 임.
       			 //	.anyRequest().hasAnyAuthority("USER","ADMIN") // JWT 기능이 안됨
       			 
                 // .anyRequest().authenticated() // 위에서 설정한 페이지를 제외한 나머지 다른 모든 페이지는 권한과는 무관하고 인증된 사용자(로그인 된 사용자)만 허용한다. 즉, 로그인 해야만 접속이 된다.
                 // .anyRequest().permitAll()     // 위에서 설정한 페이지를 제외한 나머지 다른 모든 페이지는 허용한다. 즉, 로그인을 하지 않아도 접속이 된다.  
	        )
	        
		   .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
		   // JWT 인증을 위하여 우리가 직접 만든 필터인 JwtAuthenticationFilter 를 UsernamePasswordAuthenticationFilter 전에 실행하도록 한다.
		   // JwtAuthenticationFilter 은 빈으로 설정하지 않았으므로 new JwtAuthenticationFilter(jwtTokenProvider) 으로 객체를 생성해야 함.
		   // 이로 인해 JwtAuthenticationFilter 의 doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 메서드가 자동적으로 실행되어진다.
		   //  우리가 직접 만든 JwtAuthenticationFilter 필터가 실행되는 시점은 모든 페이지 요청시 마다 JwtAuthenticationFilter 필터가 실행된다.  
		
		   // UsernamePasswordAuthenticationFilter 는 username/password 의 폼 로그인(form login) 기반 인증을 처리하는 기본 인증 필터로서  
		   // httpSecurity.formLogin() 설정이 있어야만 실행되어지는 필터이다. 
		   // 만약에 httpSecurity.formLogin() 설정이 없으면 UsernamePasswordAuthenticationFilter 는 실행되지 않는다. 
		   // 여기서는 httpSecurity.formLogin() 설정이 없기에 UsernamePasswordAuthenticationFilter 는 실행되지 않지만 
		   // UsernamePasswordAuthenticationFilter.class 를 적은 이유는 Spring Security는 내부 필터 순서 테이블을 가지고 있기 때문에,
		   // UsernamePasswordAuthenticationFilter 가 체인에 없어도 JwtAuthenticationFilter 가 들어올 순서를 잡아주는 위치 기준으로 사용하기 위함이다. 

		   /*
		      >> Spring Security 의 FilterChain 전체 구조 <<
				
				 Client
				   ↓
				 Servlet Container (Tomcat)
				   ↓
				 FilterChainProxy  ← Spring Security 진입점
				   ↓
				 SecurityFilterChain (여러 개 중 하나 선택)
				   ↓
				 각종 Security Filters 실행
				   ↓
				 DispatcherServlet
				   ↓
				 Controller
		     
		     
		       >> SecurityFilterChain 내부에 등록된 필터들이 기본 Filter 실행 순서대로 하나씩 chain.doFilter()를 통해 다음 필터가 호출된다.
		     
		       >> 기본 Filter 실행 순서 <<
		        1. DisableEncodeUrlFilter
				2. WebAsyncManagerIntegrationFilter
				3. SecurityContextHolderFilter 
				4. HeaderWriterFilter
				5. CorsFilter
				6. CsrfFilter
				7. LogoutFilter
				8. JwtAuthenticationFilter  ← (사용자 정의 필터 추가)
				9. UsernamePasswordAuthenticationFilter
				10. DefaultLoginPageGeneratingFilter
				11. DefaultLogoutPageGeneratingFilter
				12. BasicAuthenticationFilter
				13. RequestCacheAwareFilter
				14. SecurityContextHolderAwareRequestFilter
				15. AnonymousAuthenticationFilter
				16. ExceptionTranslationFilter
				17. AuthorizationFilter
				
				>> !!! 하지만 중요한 예외가 있다 !!! <<
                모든 필터가 반드시 다음 필터를 호출하는 것은 아니다.
                예를 들어:
                ★★ 로그아웃 요청일 경우 ★★
                LogoutFilter가 요청을 처리하고, 응답을 완료하면
                chain.doFilter()를 호출하지 않을 수 있다.
                → 그 아래 필터는 실행되지 않는다.

                ★★ 인증 실패 발생 시 ★★
                ExceptionTranslationFilter가 예외를 처리하면
                아래 필터로 내려가지 않는다.
                즉,
                정상 흐름에서는 위에서 아래로 순차 실행되지만, 특정 조건에서는 체인이 중단될 수 있다.!!!
		    */
		
		
		// 우리 시스템은 JWT 기반 Bearer 토큰 인증만 사용할 것이며, 
		// Basic 인증을 허용할 필요가 없으므로 BasicAuthenticationFilter를 제거하기 위해 httpBasic().disable()을 설정한다.
		// Basic 인증 => 매 요청마다 클라이언트는 ID/PW 보내어 인증을 받는 방식. 
		httpSecurity
		           .httpBasic(auth -> auth.disable());
		
		httpSecurity
			.exceptionHandling((exceptionConfig) -> exceptionConfig
					.authenticationEntryPoint(customAuthenticationEntryPoint())
					// 인증 실패시 401 에러 관련 예외처리(유효한 자격증명을 제공하지 않고 접근하려 할때 발생하는 에러이며 에러번호가 401임)
					// customAuthenticationEntryPoint() 은 맨 위에 설정해 두었음.
					
					.accessDeniedHandler(customAccessDeniedHandler())
					// 권한 실패시 403 에러 관련 예외처리(접근권한이 필요한 페이지에 접근권한이 없는 유저가 접속할 경우에 발생하는 에러이며 에러번호가 403임) 
				    // customAccessDeniedHandler() 은 맨 위에 설정해 두었음.
			); 
		
		httpSecurity
			.headers((headerConfig) -> headerConfig
						.frameOptions((frameOptionsConfig) -> frameOptionsConfig
								.sameOrigin())
			); // 동일한 사이트 주소를 가지는 src 의 iframe 만 동작하도록 허용해주는 것이다.

		/*
		  참조사이트 https://sychoi01.tistory.com/62   https://gigas-blog.tistory.com/124 
		  frameOptions() 는 HTML 태그 중에서 <iframe> 태그에서 페이지를 실행할지의 여부를 결정하는 기능인 것이다. 
		  그런데 Spring Security 에서는 Clickjacking(클릭재킹) 공격을 막기위해 기본적으로 frameOptions() 기능이 
		  "기본적으로 DENY" 로 되어져 있다. 
		  즉 위의 <iframe> 태그를 이용한 페이지 실행을 허용하지 않겠다는 의미이다.
		  Clickjacking(클릭재킹)은 웹 사용자가 자신이 클릭하고 있다고 인지하는 것과 다른 어떤 것을 클릭하게 속이는 악의적인 기법으로써, 
		  잠재적으로 공격자(해커)는 사용자(피해자)의 비밀 정보를 유출시키거나 그들(피해자)의 컴퓨터에 대한 제어를 획득할 수 있게 된다.  
		 
		  deny           ==> 어떠한 사이트에서도 frame 상에서 보여질 수 없다.
          sameorigin     ==> 동일한 사이트의 frame 에서만 보여진다. 
          allow-from uri ==> 지정된 특정 uri의 frame 에서만 보여집니다. 
		*/
		
		
		/*
           >>> Spring Security의 SessionCreationPolicy <<<
              
           SessionCreationPolicy.ALWAYS      ==>  항상 세션 생성
           SessionCreationPolicy.IF_REQUIRED ==>  필요할 때 세션 생성 (기본값)
           SessionCreationPolicy.NEVER       ==>  Spring Security는 세션 생성 안 함, 하지만 기존 세션이 있으면 사용
           SessionCreationPolicy.STATELESS	 ==>  세션을 전혀 사용하지 않음 (새로 생성 X, 기존 세션도 무시)
           
           설정하지 않으면 IF_REQUIRED가 기본값이고, 이는 대부분의 상태 기반 인증(폼 로그인 등)에서 세션을 사용하게 된다.
           REST API 처럼 세션을 필요 없는 경우, 특별히 토큰 기반 인증을 쓸 경우는 명시적으로 STATELESS로 바꾸는 것이 권장된다.  
        */
		
		// JWT를 사용하기 때문에 세션은 사용하지 않음.
		// [ CSR(SPA, Vue/React) 에서 사용 => JWT 인증 + JS에서 상태 관리 ]
        //   CSR(Client-Side Rendering) : 서버는 데이터만 JSON 으로 주고, HTML은 브라우저가 JS로 작성함. 
		//                                즉, 페이지가 처음엔 비어있고, JS가 실행되면서 화면을 채움. 
     /*
        httpSecurity
           .sessionManagement(session -> session
                  .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
     */              
                   // 세션을 생성하지도 않고, 세션을 사용하지도 않는다. (새로 생성 X, 기존 세션도 무시)
                   // 만약에 SessionCreationPolicy.STATELESS 으로 되어진 환경이라면
                   // 세션이 존재하지 않기 때문에 아래의 httpSecurity.logout() 설정은 의미가 없어서 httpSecurity.logout() 자체를 만들지 않는다.  
                   // JWT 기반 시스템에서의 로그아웃 처리는 클라이언트 측 토큰 삭제(localStorage에서 삭제, 쿠키에서 삭제) 또는 별도의 토큰 폐기 API로 로그아웃을 처리한다. 
      
		
		// SSR(Server-Side Rendering) => Thymeleaf, JSP 같은 템플릿 엔진 사용. 
		// Spring Boot + Thymeleaf 구조 → SSR 임.
        // SSR 은 서버에서 HTML 완성 → 클라이언트에 보냄 → 브라우저는 바로 표시 가능
		httpSecurity
		    .sessionManagement(session -> session
                   .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)); // 필요할 때 세션 생성 (기본값이므로 설정을 안해도 사용가능함)
        
		
		httpSecurity
	       .logout((logout) -> logout
	        		.logoutUrl("/auth/logout")   // 로그아웃 처리 URL. CSRF 가 활성화되면 기본적으로 POST만 허용함.!!! 그러므로 POST 방식으로 로그아웃 처리해야 함!!
	                .addLogoutHandler((request, response, authentication) -> {  // 로그아웃 핸들러 추가
	                     HttpSession session = request.getSession();
	                     if (session != null) {
	                         session.invalidate();
	                     }
	                   })  
	                .logoutSuccessUrl("http://localhost:8000/user-service/index") // 로그아웃 성공 후 /index 으로 redirect 함 
	                                            // 만약에 아래처럼 logoutSuccessHandler 가 있다면 위의 logoutSuccessUrl 은 효과가 없으므로 주석처리함.
	            /*  .logoutSuccessHandler((request, response, authentication) -> {
	                     response.sendRedirect("/security/member/login");
	                 })   // 로그아웃 성공 핸들러
	            */
	                .deleteCookies("refreshToken") // 로그아웃 후 삭제할 쿠키 지정
	        );
		
		return httpSecurity.build(); // 메서드로 빈을 생성하는 것이므로 return 해줘야 한다.
	}
	
	
	// 정적 리소스 허용하기
    // permitAll() 은 "필터는 타지만 인증 검사만 안 한다"는 의미이고, ignoring() 은 "아예 필터를 타지 않는다"는 의미이다. 
    // 그래서 정적 리소스 허용은 permitAll() 도 가능하지만 우리는 ignoring() 을 사용하였다. 
	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/bootstrap-4.6.2-dist/**", 
                                 "/css/**", 
                                 "/images/**", 
                                 "/js/**");  
    }
	
}
