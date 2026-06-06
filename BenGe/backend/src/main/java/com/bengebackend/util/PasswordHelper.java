package com.bengebackend.util;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHelper {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 对密码进行哈希
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    // 验证密码
    public static boolean verifyPassword(String hashedPassword, String password) {
        return encoder.matches(password, hashedPassword);
    }
}
