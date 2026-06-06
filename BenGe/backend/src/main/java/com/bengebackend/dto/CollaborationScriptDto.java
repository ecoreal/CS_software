package com.bengebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协作剧本响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationScriptDto {
    
    /**
     * 剧本标题
     */
    private String title;
    
    /**
     * 剧本内容
     */
    private String content;

    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 是否成功
     */
    private boolean success;
}
