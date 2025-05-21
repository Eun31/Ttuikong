package com.ttuikong.spring.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
		registry.addInterceptor(authenticationInterceptor).addPathPatterns("/api/**")
				.excludePathPatterns("/api/users/login", "/api/users/signup")
				.excludePathPatterns("/css/**", "/js/**", "/img/**", "/uploads/**", "/");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(loginUserArgumentResolver);
	}

	// CORS 설정
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**")
		registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*").maxAge(3600);
	}

	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/static/uploads/")
                .setCachePeriod(3600) // 1시간 캐시
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath, 
                            org.springframework.core.io.Resource location) throws java.io.IOException {
                        org.springframework.core.io.Resource resource = super.getResource(resourcePath, location);
                        
                        if (resource != null && resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        return null;
                    }
                });
	}
}
