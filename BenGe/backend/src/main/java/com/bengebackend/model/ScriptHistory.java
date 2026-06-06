package com.bengebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import lombok.Data;

@Data  // Lombok的@Data注解自动生成getter、setter、toString等方法
public class ScriptHistory {

    /**
     * 历史记录ID
     */
    private Integer id;

    /**
     * 用户发给AI的消息
     */
    private String message;

    /**
     * AI与用户交互的对话
     */
    private String response;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 脚本ID
     */
    private Integer scriptId;

    /**
     * 关联的脚本
     */
    @JsonIgnore
    private Script script;
}
