package com.devtoolmp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ServiceComb 模拟应用启动类
 * 使用标准 Spring Boot 注解
 * Schema 类模拟 ServiceComb 的 @RestSchema 功能
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.devtoolmp.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
