package com.bengebackend.controller;

import com.bengebackend.entity.UserRegisterEntity;
import com.bengebackend.service.UserLoginService;
import com.bengebackend.service.UserRegisterService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserLoginService userLoginService;
    private final UserRegisterService userRegisterService;

    @Autowired
    public UserController(UserLoginService userLoginService, UserRegisterService userRegisterService) {
        this.userLoginService = userLoginService;
        this.userRegisterService = userRegisterService;
    }

    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        System.out.println(username);
        // 检查用户名和密码是否为空
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }

        // 调用服务层的登录方法
        String token = userLoginService.LoginAsync(username, password);
        if (token == null) {
            System.out.println("登录失败~");
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        // 返回成功的响应，包含token
        return ResponseEntity.ok(new LoginResponse(token, username));
    }

    // 注册接口
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterEntity request) {
        System.out.println(request.getUsername());

        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }

        // 调用服务层的注册方法
        boolean result = userRegisterService.RegisterAsync(request.getUsername(), request.getPassword());
        if (!result) {
            return ResponseEntity.status(409).body("用户名已存在");
        }

        return ResponseEntity.ok("注册成功");
    }

    // 登录响应封装类
    @Getter
    public static class LoginResponse {
        private String token;
        private String username;

        public LoginResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }

    }
}
