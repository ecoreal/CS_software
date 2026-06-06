package com.bengebackend.entity;



public class SloganRequestEntity {

    /**
     * 用户输入的提示
     */
    private String prompt = "";  // 默认值为空字符串

    /**
     * Slogan的ID
     */
    private Integer scriptId;

    // 默认构造函数
    public SloganRequestEntity() {}

    // 带参构造函数
    public SloganRequestEntity(String prompt, Integer scriptId) {
        this.prompt = prompt;
        this.scriptId = scriptId;
    }

    // Getter 和 Setter
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }
}
