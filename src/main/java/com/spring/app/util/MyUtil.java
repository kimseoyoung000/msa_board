package com.spring.app.util;

import jakarta.servlet.http.HttpServletRequest;

public class MyUtil {

	// *** ? 다음의 데이터까지 포함한 현재 URL 주소를 알려주는 메소드를 생성 *** //
	public static String getCurrentURL(HttpServletRequest request) { 
		
		String currentURL = request.getRequestURL().toString();
	 //	System.out.println("currentURL => " + currentURL);
	 // currentURL => http://localhost:9090/MyMVC/member/memberList.up 
		
		String queryString = request.getQueryString();
	 // System.out.println("queryString => " + queryString);
	 //	queryString => searchType=name&searchWord=%EC%9C%A0&sizePerPage=5&currentShowPageNo=15
	 //	               searchType=name&searchWord=유&sizePerPage=5&currentShowPageNo=15
	 // queryString => null (POST 방식일 경우)
		
		if(queryString != null) { // GET 방식일 경우
			currentURL += "?"+queryString;
	     // currentURL => http://localhost:8002/board/list?searchType=subject&searchWord=java
		}
		
	//	System.out.println("~~~~~~~~~~~요기요");
	//	System.out.println(currentURL);
	//  http://localhost:8002/board/list?searchType=subject&searchWord=java
		
		String[] arrurl = currentURL.split(":8002");
		// {"http://localhost","/board/list?searchType=subject&searchWord=java"}
		currentURL = arrurl[0]+":8000/board-service"+arrurl[1];
		
	//	System.out.println("currentURL => " + currentURL); 
	//	currentURL => http://localhost:8000/board-service/board/list?searchType=subject&searchWord=java
		
		return currentURL;
		
	}// end of public static String getCurrentURL(HttpServletRequest request)-----------	
	
}
