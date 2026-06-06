package com.bengebackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private long jwtExpiration = 360000000;  // 过期时间（例如1小时）

    // 解析 Token
    public Claims parseToken(String token) throws SignatureException {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret)  // 设置密钥来验证签名
                    .parseClaimsJws(token)  // 解析 Token
                    .getBody();  // 获取有效载荷
        } catch (SignatureException e) {
            throw new SignatureException("Invalid JWT signature.");
        }
    }

    // 获取用户ID
    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);

        System.out.println("用户ID："+claims.getSubject());
        return Integer.parseInt(claims.getSubject());  // 从 Token 中提取用户 ID
    }

    // 获取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);

        System.out.println("用户名："+claims.get("username", String.class));
        return claims.get("username", String.class);  // 从 Token 中提取用户名
    }

    // 验证 Token 是否过期
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());  // 判断过期时间
    }
}
