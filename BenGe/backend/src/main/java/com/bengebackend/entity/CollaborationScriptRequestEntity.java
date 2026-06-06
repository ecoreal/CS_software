package com.bengebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协作剧本生成请求实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationScriptRequestEntity {

    /**
     * 房间ID
     */
    private Integer roomId;
    /**
     * 协作设计的上下文数据（JSON字符串）
     */
    private String contextData;
}
