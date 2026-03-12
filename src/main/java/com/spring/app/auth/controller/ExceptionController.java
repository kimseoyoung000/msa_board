package com.spring.app.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/exception/")
public class ExceptionController {

	// 401 Unauthorized (인증 오류)
	@GetMapping("noAuthentication")
	public String noAuthenticated() {
		return "exception/noAuthentication";
	}
	
	// 403 Forbidden (권한 오류)
	@GetMapping("accessDeny")
	public String noAuthorized() {
		return "exception/accessDeny";
	}
	
}
