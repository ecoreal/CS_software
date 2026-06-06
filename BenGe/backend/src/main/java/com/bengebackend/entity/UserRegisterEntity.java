package com.bengebackend.entity;

public class UserRegisterEntity {

    private String username;
    private String password;

    // 默认构造函数
    public UserRegisterEntity() {}

    // 带参构造函数
    public UserRegisterEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter 和 Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
