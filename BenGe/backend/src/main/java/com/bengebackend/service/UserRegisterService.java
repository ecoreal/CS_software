package com.bengebackend.service;

public interface UserRegisterService {
    Boolean RegisterAsync(String username, String password); // 返回注册是否成功
    Boolean CheckUsernameExistsAsync(String username); // 检查用户名是否已存在
}
