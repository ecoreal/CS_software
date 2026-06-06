package com.bengebackend.entity;



public class ScriptVisualRequestEntity {

    /**
     * 剧本ID
     */
    private Integer scriptId;

    /**
     * 元素ID
     */
    private Integer elementId;

    // 默认构造函数
    public ScriptVisualRequestEntity() {}

    // 带参构造函数
    public ScriptVisualRequestEntity(Integer scriptId, Integer elementId) {
        this.scriptId = scriptId;
        this.elementId = elementId;
    }

    // Getter 和 Setter
    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }

    public Integer getElementId() {
        return elementId;
    }

    public void setElementId(Integer elementId) {
        this.elementId = elementId;
    }
}
