package com.ttuikong.spring.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.ttuikong.spring.model.dao")
public class DBConfig {

}
