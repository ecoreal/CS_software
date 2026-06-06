package com.bengebackend.service.serviceImpl;

import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.service.UserLoginService;
import com.bengebackend.util.PasswordHelper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private UserMapper userMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public String LoginAsync(String username, String plainPassword) {
        // 查询用户
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        // 使用原本的PasswordHelper进行密码验证
        if (!PasswordHelper.verifyPassword(user.getPasswordHash(), plainPassword)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 生成JWT令牌
        return generateJwtToken(user);
    }

    private String generateJwtToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();
        Date expirationDate = new Date(currentTimeMillis + jwtExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString()) // Subject是用户ID
                .claim("username", user.getUsername()) // 存储用户名
                .setIssuedAt(new Date(currentTimeMillis)) // 当前时间
                .setExpiration(expirationDate) // 设置过期时间
                .signWith(SignatureAlgorithm.HS256, jwtSecret) // 使用密钥生成签名
                .compact();
    }
}
