package com.bengebackend.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class ScriptFrameworkRequestEntity {

    /**
     * 脚本ID
     */
    @NotNull(message = "ScriptId cannot be null")
    private Integer scriptId;

    /**
     * 用户自定义提示信息
     */
    @Size(max = 500, message = "UserPrompt length must be less than 500 characters")
    private String userPrompt;

    // 默认构造函数
    public ScriptFrameworkRequestEntity() {}

    // 带参构造函数
    public ScriptFrameworkRequestEntity(Integer scriptId, String userPrompt) {
        this.scriptId = scriptId;
        this.userPrompt = userPrompt;
    }

    // Getter 和 Setter
    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }
}
