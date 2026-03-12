package com.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*
  Spring Security 라이브러리를 추가한 경우 Spring Security 기능이 추가되어 사이트에 접속하면 기본적으로 login 화면이 실행한다. 
  스프링 부트의 Spring Security 기능을 제거하기 위해서는 아래처럼 @SpringBootApplication 애노테이션에
  exclude = SecurityAutoConfiguration.class 를 추가하면 된다.
*/
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableDiscoveryClient // Discovery Server 의 클라이언트로 사용함.
                       // (즉, Micro Service 로 사용하겠다는 말이다.)
public class MsaBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsaBoardApplication.class, args);
	}

}
