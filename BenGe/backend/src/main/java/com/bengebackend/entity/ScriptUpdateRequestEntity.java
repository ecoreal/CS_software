package com.bengebackend.entity;


public class ScriptUpdateRequestEntity {

    /**
     * 脚本ID
     */
    private Integer scriptId;

    /**
     * 内容
     */
    private String content;

    /**
     * 阶段，默认为1
     */
    private int stage = 1;

    // 默认构造函数
    public ScriptUpdateRequestEntity() {}

    // 带参构造函数
    public ScriptUpdateRequestEntity(Integer scriptId, String content, int stage) {
        this.scriptId = scriptId;
        this.content = content;
        this.stage = stage;
    }

    // Getter 和 Setter
    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
