package com.ttuikong.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("뛰콩 REST API")
                        .description("Spring Boot을 이용한 뛰콩 REST API 명세서입니다.")
                        .version("v1.0.0")
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0"))
                        );
                // JWT 인증 설정 제거
                // .addSecurityItem(new SecurityRequirement().addList("JWT"))
                // .components(new Components()
                //         .addSecuritySchemes("JWT", new SecurityScheme()
                //                 .type(SecurityScheme.Type.HTTP)
                //                 .scheme("bearer")
                //                 .bearerFormat("JWT")
                //                 .in(SecurityScheme.In.HEADER)
                //                 .name("Authorization"))
                //         );
    }
}
