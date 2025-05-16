package com.ttuikong.spring.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	// resolver 적용 가능 유무 판단
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @LoginUser 어노테이션이 붙어있고, 해당 파라미터의 타입이 User이면 사용
        return parameter.hasParameterAnnotation(LoginUser.class) && parameter.getParameterType().equals(User.class);
    }

    // resolver가 주어진 parameter에 주입할 객체를 처리
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // JWT 인증 인터셉터에서 설정한 loginUser 속성을 가져옴
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        // request 속성에서 인증된 사용자 정보 가져오기
        return request.getAttribute("loginUser");
    }
}
