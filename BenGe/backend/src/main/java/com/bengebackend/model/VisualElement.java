package com.bengebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import lombok.Data;

@Data  // Lombok的@Data注解自动生成getter、setter、toString等方法
public class VisualElement {

    /**
     * 元素ID
     */
    private Integer id;

    /**
     * 元素类型：Character / Scene / Prop
     */
    private String type;

    /**
     * 元素名称
     */
    private String name;

    /**
     * 元素描述
     */
    private String description;

    /**
     * 元素图片URL
     */
    private String imageUrl;

    /**
     * 图片生成时间
     */
    private LocalDateTime imageGeneratedAt;

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
