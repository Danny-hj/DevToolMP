package com.devtoolmp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 公开的API端点 - 不需要认证
                .requestMatchers(
                    "/tools/**",
                    "/tools",
                    "/tools/search",
                    "/tools/ranking/**",
                    "/categories/**",
                    "/tags/**"
                ).permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(null));

        return http.build();
    }
}
