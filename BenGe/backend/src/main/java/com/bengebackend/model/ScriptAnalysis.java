package com.bengebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import lombok.Data;

@Data  // Lombok的@Data注解自动生成getter、setter、toString等方法
public class ScriptAnalysis {

    /**
     * 脚本分析ID
     */
    private Integer id;

    /**
     * 分析结果
     */
    private String analysisResult;

    /**
     * 分析时间
     */
    private LocalDateTime analyzedAt;

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