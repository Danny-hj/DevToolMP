package com.devtoolmp.service;

import com.devtoolmp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从 Authorization header 中提取用户 ID
     * @param authHeader Authorization header value (格式: "Bearer {token}")
     * @return 用户 ID,如果无法解析则返回 null
     */
    public Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Authorization header 中提取用户名
     * @param authHeader Authorization header value (格式: "Bearer {token}")
     * @return 用户名,如果无法解析则返回 null
     */
    public String extractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 token 是否有效
     * @param authHeader Authorization header value (格式: "Bearer {token}")
     * @return token 是否有效
     */
    public boolean isTokenValid(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        try {
            return !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
