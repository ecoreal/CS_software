package com.bengebackend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Context {
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                // 获取存储在 authentication 中的 userId（保存在 details 字段）
                Integer userId = (Integer) authentication.getDetails();  // 强制转换为 Integer 类型
                return userId;  // 返回 userId
            } catch (Exception e) {
                System.out.println("Error retrieving userId: " + e.getMessage());
                return 1;  // 如果出错，返回默认的 1
            }
        }

        return 1;  // 如果未认证，返回默认的 1
    }
}
