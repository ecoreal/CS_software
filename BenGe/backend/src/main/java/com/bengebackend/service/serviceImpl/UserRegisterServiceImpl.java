package com.bengebackend.service.serviceImpl;

import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.service.UserRegisterService;
import com.bengebackend.util.PasswordHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRegisterServiceImpl implements UserRegisterService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Boolean RegisterAsync(String username, String password) {
        // 检查用户名是否已存在
        if (CheckUsernameExistsAsync(username)) {
            return false; // 用户名已存在，注册失败
        }
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(PasswordHelper.hashPassword(password)); // 使用密码哈希函数
        // 插入新用户
        return userMapper.insertUser(newUser) > 0;
    }

    @Override
    public Boolean CheckUsernameExistsAsync(String username) {
        Optional<User> existingUser = Optional.ofNullable(userMapper.findByUsername(username).orElse(null));
        return existingUser.isPresent(); // 如果找到了用户，则用户名已存在
    }
}
