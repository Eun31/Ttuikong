package com.ttuikong.spring.interceptor;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.UserService;
import com.ttuikong.spring.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// @LoginRequired 어노테이션 체크
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();

		// @LoginRequired 어노테이션이 없다면 인증 검사 생략
		if (!method.isAnnotationPresent(LoginRequired.class)) {
			return true;
		}

		// Authorization 헤더에서 JWT 토큰을 추출
		String authorizationHeader = request.getHeader("Authorization");

		// 토큰이 없거나 Bearer 형식이 아닌 경우
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			// API 요청인 경우
			if (request.getRequestURI().startsWith("/api/")) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Unauthorized: No valid JWT token\"}");
				return false;
			}

			// 웹페이지 요청인 경우
			response.sendRedirect("/login");
			return false;
		}

		// Bearer 접두사 제거
		String token = authorizationHeader.substring(7);

		// 토큰 유효성 검증
		if (!jwtUtil.validateToken(token)) {
			// API 요청인 경우
			if (request.getRequestURI().startsWith("/api/")) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Unauthorized: Invalid or expired JWT token\"}");
				return false;
			}

			// 웹페이지 요청인 경우
			response.sendRedirect("/login");
			return false;
		}

		// 토큰에서 사용자 정보 추출
		String email = jwtUtil.getEmailFromToken(token);
		Integer userId = jwtUtil.getUserIdFromToken(token);

		// 사용자 정보를 요청 속성에 저장
//        User user = new User();
//        user.setId(userId);
		User user = userService.getUserById(userId);

		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\":\"Unauthorized: User not found\"}");
			return false;
		}

		// request 속성에 인증된 사용자 정보 저장
		request.setAttribute("loginUser", user);
		
		return true;
	}
}
