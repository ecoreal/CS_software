package com.bengebackend.entity;


public class ScriptStage3Entity {

    /**
     * 脚本ID
     */
    private Integer scriptId;

    /**
     * 是否生成可视化元素
     */
    private Boolean isGenVisualElement;

    // 默认构造函数
    public ScriptStage3Entity() {}

    // 带参构造函数
    public ScriptStage3Entity(Integer scriptId, Boolean isGenVisualElement) {
        this.scriptId = scriptId;
        this.isGenVisualElement = isGenVisualElement;
    }

    // Getter 和 Setter
    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }

    public Boolean getIsGenVisualElement() {
        return isGenVisualElement;
    }

    public void setIsGenVisualElement(Boolean isGenVisualElement) {
        this.isGenVisualElement = isGenVisualElement;
    }
}
