package com.spring.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement // 스프링 부트에서 Transaction 처리를 위한 용도
public class QueryDSLConfig {

	private final EntityManager em;
	/*
	 EntityManager em 이 자동적으로 bean 으로 올라온 이유는 다음과 같다.
	 
	 build.gradle 에서
	 
	 dependencies {
	   implementation 'org.springframework.boot:spring-boot-starter-data-jpa' 
	 }
	 의존성을 넣어줌으로서 Spring Boot는 JPA 관련 자동 설정을 수행해준다.
	 이러한 JPA 관련 자동 설정으로 인하여 EntityManager 가 생성되어지고,
	 Spring 은 EntityManager 를 스프링 컨테이너에 Bean으로 등록해놓고, 이것을 필요한 곳에 주입해주는 것이다.
	   
	 */
	
	// Query DSL 을 실행하는 객체(bean) jPAQueryFactory 생성하기  
	@Bean
	public JPAQueryFactory jPAQueryFactory() {
		return new JPAQueryFactory(em);
	}
	
	// Transaction Manager 생성하기 (JPA 는 Transaction Manager 의 빈이름이 무조건 transactionManager 이어야 한다.!!) 
	@Bean(name = "transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
	    return new JpaTransactionManager(emf);
	}
}
