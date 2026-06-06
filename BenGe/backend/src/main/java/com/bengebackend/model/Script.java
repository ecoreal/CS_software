package com.bengebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data // Lombok的@Data注解会自动生成getter/setter/toString等方法
public class Script {

    private Integer id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime lastUpdated;
    private Integer userId;

    @JsonIgnore
    private User user; // 关联到 User 实体类

    private List<ScriptHistory> scriptHistories;
    private List<VisualElement> visualElements;
    private ScriptAnalysis scriptAnalysis;

    private Integer stage = 1;
}
