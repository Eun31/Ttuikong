package com.ttuikong.spring.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ttuikong.spring.interceptor.AuthenticationInterceptor;
import com.ttuikong.spring.resolver.LoginUserArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    
    @Autowired
    private LoginUserArgumentResolver loginUserArgumentResolver;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**") 
            .excludePathPatterns(
                "/api/users/login",   
                "/api/users/signup"     
            )
            .excludePathPatterns("/css/**", "/js/**", "/img/**", "/");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
    
    // CORS 설정 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**")
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
