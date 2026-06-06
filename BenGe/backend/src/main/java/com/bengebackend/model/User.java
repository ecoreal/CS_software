package com.bengebackend.model;

import java.util.List;

import lombok.Data;

@Data  // Lombok的@Data注解自动生成getter、setter、toString等方法
public class User {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希值
     */
    private String passwordHash;

    /**
     * 用户拥有的脚本
     */
    private List<Script> scripts;
}
