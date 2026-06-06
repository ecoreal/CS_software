package com.bengebackend.websocket.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * WebSocket 消息统一封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /** 消息类型 */
    private String type;

    /** 房间 ID */
    private String roomId;

    /** 用户 ID */
    private Integer userId;

    /** 用户名 */
    private String username;

    /** 消息文本内容 */
    private String content;

    /** 消息时间戳 */
    private String time;

    /** 用户头像 URL */
    private String avatar;

    /** 用户认证 Token（用于 auth 类型消息） */
    private String token;

    /** 选择关键词数组 */
    private List<String> key;

    /** 确认选择的关键词 */
    private boolean hasChosen;

    /** 投票数量统计 */
    private List<Integer> vote;

    /** 确认是否投票 */
    private boolean hasVoted;

    /** 角色名称（如“剧情设计师”“人物设计师”） */
    private String roleName;

    /** 剧情设计师节点数据列表 */
    private List<NodeData> nodes;

    /** 剧情设计师边数据列表 */
    private List<EdgeData> edges;

    /** 人物设计师节点数据列表 */
    private List<CharacterNode> characterNodes;

    /** 人物设计师边数据列表 */
    private List<CharacterEdge> characterEdges;

    /** 线索设计师节点数据列表 */
    private List<ClueNode> clueNodes;

    /** 推断结点数据列表 */
    private List<InferenceNode> inferenceNodes;

    /**
     * 线索设计师 - 人物结点列表
     */
    private List<PersonNode> personNodes;

    /** 场景设计师 - 氛围节点列表 */
    private List<AtmosphereNode> atmosphereNodes;

    /** 场景设计师 - 氛围边列表 */
    private List<AtmosphereEdge> atmosphereEdges;

    /**
     * 剧情设计师节点结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeData {
        /** 节点 ID */
        private String id;

        /** 节点类型 */
        private String type;

        /** 节点位置 */
        private Position position;

        /** 节点内容 */
        private NodeContent data;
    }

    /**
     * 节点位置信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        /** x 坐标 */
        private Integer x;

        /** y 坐标 */
        private Integer y;
    }

    /**
     * 剧情设计师节点内容结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeContent {
        /** 节点标题 */
        private String title;

        /** 时间标签 */
        private String timeLabel;

        /** 涉及人物 */
        private String characters;

        /** 相关线索 */
        private String clues;

        /** 场景描述 */
        private String sceneDescription;

        /** 连接信息 */
        private String nodeConnections;

        /** 备注信息 */
        private String notes;
    }

    /**
     * 剧情设计师边结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，保持向后兼容
    public static class EdgeData {
        /** 边 ID */
        private String id;

        /** 源节点 ID */
        private String source;

        /** 目标节点 ID */
        private String target;

        /** 源节点连接点位置 */
        private String sourcePosition;

        /** 目标节点连接点位置 */
        private String targetPosition;

        /** 源节点连接句柄（前端兼容字段） */
        private String sourceHandle;

        /** 目标节点连接句柄（前端兼容字段） */
        private String targetHandle;

        /** 边类型 */
        private String type;

        /** 边内容 */
        private EdgeContent data;
    }

    /**
     * 剧情设计师边内容结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EdgeContent {
        /** 关系类型 */
        private String type;

        /** 标签内容 */
        private String label;
    }

    /**
     * 人物设计师节点结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CharacterNode {
        /** 节点 ID */
        private String id;

        /** 节点类型 */
        private String type;

        /** 节点位置 */
        private Position position;

        /** 人物节点内容 */
        private CharacterContent data;
    }

    /**
     * 人物节点详细内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CharacterContent {
        /** 姓名 */
        private String name;

        /** 头像 URL */
        private String avatar;

        /** 年龄 */
        private Integer age;

        /** 职业 */
        private String occupation;

        /** 性格特征列表 */
        private List<String> personality;

        /** 背景故事 */
        private String background;

        /** 技能列表 */
        private List<String> skills;

        /** 携带物品 */
        private String items;

        /** 备注信息 */
        private String notes;

        /** 关系 ID 列表 */
        private List<Relationship> relationships;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Relationship {
        /** 目标节点 ID */
        private String targetId;

        /** 关系类型（如 friend, enemy） */
        private String type;

        /** 关系描述 */
        private String description;

        /** 关系强度（如 1-10 分） */
        private int strength;

        /** 当前状态（如 active、inactive） */
        private String status;
    }

    /**
     * 人物设计师边结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CharacterEdge {
        /** 边 ID */
        private String id;

        /** 源人物节点 ID */
        private String source;

        /** 目标人物节点 ID */
        private String target;

        /** 源连接点位置 */
        private String sourceHandle;

        /** 目标连接点位置 */
        private String targetHandle;

        /** 边类型（如 relationship） */
        private String type;

        /** 边数据 */
        private CharacterEdgeContent data;
    }

    /**
     * 人物设计师边内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，保持向后兼容
    public static class CharacterEdgeContent {
        // === 角色-角色关系字段 ===
        /** 关系类型（如亲属、敌对） */
        private String type;

        /** 关系描述 */
        private String description;

        /** 关系强度（如高、中、低） */
        private String strength;

        /** 当前状态（如稳定、紧张） */
        private String status;

        /** 显示用标签 */
        private String label;

        // === 角色-场景关系字段 ===
        /** 参与类型（如主角、配角、反派等） - 用于角色-场景关系 */
        private String participationType;

        /** 重要性（如关键、高、普通、低） - 用于角色-场景关系 */
        private String importance;

        /** 边样式（如实线、虚线） - 用于角色-场景关系 */
        private String style;

        /** 是否显示标签 - 用于角色-场景关系 */
        private Boolean showLabel;
    }

    /**
     * 线索设计师结点结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClueNode {
        private String id;
        private String type;
        private Position position;
        private ClueNodeContent data;
    }

    /**
     * 线索设计师结点内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClueNodeContent {
        private String title;
        private String relatedEvent;
        private String detail;
        private String logic;
        private String tags;
        private String notes;
    }

    /**
     * 线索设计师 - 推理结点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InferenceNode {
        private String id;
        private String type;
        private Position position;
        private InferenceContent data;
    }

    /**
     * 线索设计师 - 推理内容结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InferenceContent {
        private String title;
        private String summary;
        private String evidence;
        private String tags;
        private String notes;
    }

    /**
     * 线索设计师 - 人物结点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonNode {
        private String id;
        private String type;
        private Position position;
        private PersonContent data;
    }

    /**
     * 人物节点内容结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonContent {
        private String name;
        private String bio;
        private List<String> clues;
        private List<String> tags;
        private String notes;
    }

    /**
     * 场景设计师 - 氛围节点结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtmosphereNode {
        private String id;
        private String type;
        private Position position;
        private AtmosphereContent data;
    }

    /**
     * 场景设计师 - 氛围节点内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtmosphereContent {
        private String title;      // 节点标题
        private String timeLabel;  // 时间标签
        private String mood;       // 情绪氛围（如：平静、紧张）
        private String lighting;   // 灯光设置
        private String music;      // 背景音乐
        private String weather;    // 天气
        private String notes;      // 备注
    }

    /**
     * 场景设计师 - 氛围边结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtmosphereEdge {
        private String id;
        private String source;
        private String target;
        private String type;
        private AtmosphereEdgeContent data;
    }

    /**
     * 场景设计师 - 氛围边内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtmosphereEdgeContent {
        private String type;    // 边类型，如 atmosphere-influence
        private String label;   // 显示标签
        private String style;   // 线型，如 dashed
        private String color;   // 颜色值，如 #ff6b6b
    }

}