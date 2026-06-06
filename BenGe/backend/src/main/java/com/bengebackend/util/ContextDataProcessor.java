package com.bengebackend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上下文数据处理器
 * 用于解析前端传递的上下文数据并生成AI友好的描述
 */
@Slf4j
@Component
public class ContextDataProcessor {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析并生成完整的上下文摘要
     * @param contextDataJson 前端传递的JSON字符串
     * @param designerType 设计师类型（用于日志记录，不影响内容生成）
     * @return 格式化的完整上下文描述
     */
    public String generateContextSummary(String contextDataJson, String designerType) {
        if (contextDataJson == null || contextDataJson.trim().isEmpty()) {
            return "当前协作环境：新项目，暂无现有内容。";
        }

        try {
            JsonNode contextData = objectMapper.readTree(contextDataJson);
            StringBuilder summary = new StringBuilder();

            // 添加协作环境信息
            addCollaborationInfo(summary, contextData);

            // 添加所有类型的现有内容
            addAllExistingContent(summary, contextData);

            // 添加完整的讨论历史
            addCompleteDiscussion(summary, contextData);

            log.info(summary.toString());

            return summary.toString();

        } catch (Exception e) {
            log.error("解析上下文数据失败，设计师类型: {}, 数据: {}", designerType, contextDataJson, e);
            return "当前协作环境：数据解析异常，建议基于用户需求进行创作。";
        }
    }
    
    /**
     * 添加协作环境信息
     */
    private void addCollaborationInfo(StringBuilder summary, JsonNode contextData) {
        JsonNode context = contextData.get("context");
        if (context != null) {
            JsonNode room = context.get("room");
            JsonNode user = context.get("user");
            JsonNode statistics = context.get("statistics");
            
            summary.append("【协作环境】\n");
            
            if (room != null) {
                summary.append("- 房间成员：").append(room.get("members").asInt()).append("人\n");
                summary.append("- 协作阶段：").append(room.get("stage").asText()).append("\n");
            }
            
            if (statistics != null) {
                summary.append("- 当前进度：已创建")
                       .append(statistics.get("nodeCount").asInt()).append("个节点，")
                       .append(statistics.get("edgeCount").asInt()).append("个连接\n");
                summary.append("- 讨论消息：").append(statistics.get("chatCount").asInt()).append("条\n");
            }
            
            summary.append("\n");
        }
    }
    
    /**
     * 添加所有现有内容的完整信息
     */
    private void addAllExistingContent(StringBuilder summary, JsonNode contextData) {
        summary.append("【项目现有内容】\n");

        JsonNode nodes = contextData.get("nodes");
        if (nodes == null) {
            summary.append("暂无任何设计内容，这是项目的初始阶段。\n\n");
            return;
        }

        boolean hasContent = false;

        // 场景内容
        JsonNode sceneNodes = nodes.get("scene");
        if (sceneNodes != null && sceneNodes.isArray() && sceneNodes.size() > 0) {
            hasContent = true;
            summary.append("场景设计（").append(sceneNodes.size()).append("个）：\n");
            for (JsonNode scene : sceneNodes) {
                JsonNode data = scene.get("data");
                if (data != null) {
                    String title = safeGetText(data, "title", "未命名场景");
                    String timeLabel = safeGetText(data, "timeLabel", "时间待定");
                    String sceneDescription = safeGetText(data, "sceneDescription", "描述待定");
                    String characters = safeGetArrayAsText(data, "characters");
                    String clues = safeGetArrayAsText(data, "clues");
                    String notes = safeGetText(data, "notes", "");

                    summary.append("- ").append(title)
                           .append("（").append(timeLabel).append("）")
                           .append("：").append(sceneDescription);

                    if (!characters.isEmpty()) {
                        summary.append("，涉及角色：").append(characters);
                    }
                    if (!clues.isEmpty()) {
                        summary.append("，相关线索：").append(clues);
                    }
                    if (!notes.isEmpty()) {
                        summary.append("，备注：").append(notes);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 角色内容
        JsonNode characterNodes = nodes.get("character");
        if (characterNodes != null && characterNodes.isArray() && characterNodes.size() > 0) {
            hasContent = true;
            summary.append("角色设计（").append(characterNodes.size()).append("个）：\n");
            for (JsonNode character : characterNodes) {
                JsonNode data = character.get("data");
                if (data != null) {
                    String name = safeGetText(data, "name", "未命名角色");
                    String occupation = safeGetText(data, "occupation", "职业待定");
                    String background = safeGetText(data, "background", "背景待定");
                    String personality = safeGetArrayAsText(data, "personality");
                    String skills = safeGetArrayAsText(data, "skills");
                    String items = safeGetText(data, "items", "");
                    String notes = safeGetText(data, "notes", "");

                    summary.append("- ").append(name)
                           .append("（").append(occupation).append("）")
                           .append("：").append(background);

                    if (!personality.isEmpty()) {
                        summary.append("，性格特点：").append(personality);
                    }
                    if (!skills.isEmpty()) {
                        summary.append("，技能：").append(skills);
                    }
                    if (!items.isEmpty()) {
                        summary.append("，物品：").append(items);
                    }
                    if (!notes.isEmpty()) {
                        summary.append("，备注：").append(notes);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 线索内容
        JsonNode clueNodes = nodes.get("clue");
        if (clueNodes != null && clueNodes.isArray() && clueNodes.size() > 0) {
            hasContent = true;
            summary.append("线索设计（").append(clueNodes.size()).append("个）：\n");
            for (JsonNode clue : clueNodes) {
                JsonNode data = clue.get("data");
                if (data != null) {
                    String title = safeGetText(data, "title", "未命名线索");
                    String detail = safeGetText(data, "detail", "");
                    String relatedEvent = safeGetText(data, "relatedEvent", "");
                    String logic = safeGetText(data, "logic", "");
                    String tags = safeGetText(data, "tags", "");
                    String note = safeGetText(data, "note", "");

                    summary.append("- ").append(title);
                    if (!detail.isEmpty()) {
                        summary.append("：").append(detail);
                    }
                    if (!relatedEvent.isEmpty()) {
                        summary.append("，相关事件：").append(relatedEvent);
                    }
                    if (!logic.isEmpty()) {
                        summary.append("，推理逻辑：").append(logic);
                    }
                    if (!tags.isEmpty()) {
                        summary.append("，标签：").append(tags);
                    }
                    if (!note.isEmpty()) {
                        summary.append("，备注：").append(note);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 氛围内容
        JsonNode atmosphereNodes = nodes.get("atmosphere");
        if (atmosphereNodes != null && atmosphereNodes.isArray() && atmosphereNodes.size() > 0) {
            hasContent = true;
            summary.append("氛围设计（").append(atmosphereNodes.size()).append("个）：\n");
            for (JsonNode atmosphere : atmosphereNodes) {
                JsonNode data = atmosphere.get("data");
                if (data != null) {
                    String title = safeGetText(data, "title", "未命名氛围");
                    String mood = safeGetText(data, "mood", "情绪待定");
                    String lighting = safeGetText(data, "lighting", "待定");
                    String music = safeGetText(data, "music", "待定");
                    String weather = safeGetText(data, "weather", "待定");
                    String description = safeGetText(data, "description", "");

                    summary.append("- ").append(title)
                           .append("（").append(mood).append("）")
                           .append("：灯光").append(lighting)
                           .append("，音乐").append(music)
                           .append("，天气").append(weather);

                    if (!description.isEmpty()) {
                        summary.append("，描述：").append(description);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 其他类型节点（推理、人物等）
        addOtherNodeTypes(summary, nodes);

        if (!hasContent) {
            summary.append("暂无任何设计内容，这是项目的初始阶段。\n\n");
        }

        // 添加节点间关系信息
        addNodeRelationships(summary, contextData);
    }
    
    /**
     * 添加其他类型的节点信息
     */
    private void addOtherNodeTypes(StringBuilder summary, JsonNode nodes) {
        // 推理节点
        JsonNode inferenceNodes = nodes.get("inference");
        if (inferenceNodes != null && inferenceNodes.isArray() && inferenceNodes.size() > 0) {
            summary.append("推理内容（").append(inferenceNodes.size()).append("个）：\n");
            for (JsonNode inference : inferenceNodes) {
                JsonNode data = inference.get("data");
                if (data != null) {
                    String title = safeGetText(data, "title", "未命名推理");
                    String summary_text = safeGetText(data, "summary", "");
                    String evidence = safeGetText(data, "evidence", "");
                    String tags = safeGetText(data, "tags", "");

                    summary.append("- ").append(title);
                    if (!summary_text.isEmpty()) {
                        summary.append("：").append(summary_text);
                    }
                    if (!evidence.isEmpty()) {
                        summary.append("，证据：").append(evidence);
                    }
                    if (!tags.isEmpty()) {
                        summary.append("，标签：").append(tags);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 人物节点
        JsonNode personNodes = nodes.get("person");
        if (personNodes != null && personNodes.isArray() && personNodes.size() > 0) {
            summary.append("人物信息（").append(personNodes.size()).append("个）：\n");
            for (JsonNode person : personNodes) {
                JsonNode data = person.get("data");
                if (data != null) {
                    String name = safeGetText(data, "name", "未命名人物");
                    String bio = safeGetText(data, "bio", "");
                    String clues = safeGetArrayAsText(data, "clues");
                    String tags = safeGetArrayAsText(data, "tags");

                    summary.append("- ").append(name);
                    if (!bio.isEmpty()) {
                        summary.append("：").append(bio);
                    }
                    if (!clues.isEmpty()) {
                        summary.append("，相关线索：").append(clues);
                    }
                    if (!tags.isEmpty()) {
                        summary.append("，标签：").append(tags);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }
    }

    /**
     * 添加节点间关系信息
     */
    private void addNodeRelationships(StringBuilder summary, JsonNode contextData) {
        summary.append("【节点关系网络】\n");

        JsonNode edges = contextData.get("edges");
        JsonNode nodes = contextData.get("nodes");

        if (edges == null || nodes == null) {
            summary.append("暂无节点关系连接。\n\n");
            return;
        }

        // 创建节点ID到节点信息的映射
        Map<String, String> nodeIdToName = createNodeIdMapping(nodes);

        boolean hasRelationships = false;

        // 处理角色关系
        JsonNode characterEdges = edges.get("character");
        if (characterEdges != null && characterEdges.isArray() && characterEdges.size() > 0) {
            hasRelationships = true;
            summary.append("角色关系（").append(characterEdges.size()).append("个）：\n");
            for (JsonNode edge : characterEdges) {
                String sourceName = nodeIdToName.getOrDefault(safeGetText(edge, "source", ""), "未知角色");
                String targetName = nodeIdToName.getOrDefault(safeGetText(edge, "target", ""), "未知角色");
                String relationship = safeGetText(edge, "relationship", "关系");

                JsonNode edgeData = edge.get("data");
                if (edgeData != null) {
                    String type = safeGetText(edgeData, "type", "");
                    String strength = safeGetText(edgeData, "strength", "");
                    String status = safeGetText(edgeData, "status", "");
                    String description = safeGetText(edgeData, "description", "");

                    summary.append("- ").append(sourceName).append(" ↔ ").append(targetName)
                           .append("：").append(getRelationshipTypeText(type));

                    if (!strength.isEmpty()) {
                        summary.append("（强度：").append(strength).append("）");
                    }
                    if (!status.isEmpty() && !"active".equals(status)) {
                        summary.append("（状态：").append(getStatusText(status)).append("）");
                    }
                    if (!description.isEmpty()) {
                        summary.append("，").append(description);
                    }
                    summary.append("\n");
                } else {
                    summary.append("- ").append(sourceName).append(" ↔ ").append(targetName)
                           .append("：").append(relationship).append("\n");
                }
            }
            summary.append("\n");
        }

        // 处理场景关系
        JsonNode sceneEdges = edges.get("scene");
        if (sceneEdges != null && sceneEdges.isArray() && sceneEdges.size() > 0) {
            hasRelationships = true;
            summary.append("场景关系（").append(sceneEdges.size()).append("个）：\n");
            for (JsonNode edge : sceneEdges) {
                String sourceName = nodeIdToName.getOrDefault(safeGetText(edge, "source", ""), "未知节点");
                String targetName = nodeIdToName.getOrDefault(safeGetText(edge, "target", ""), "未知节点");
                String relationship = safeGetText(edge, "relationship", "连接");
                String description = safeGetText(edge, "description", "");

                JsonNode edgeData = edge.get("data");
                if (edgeData != null) {

                    String participationType = safeGetText(edgeData, "participationType", "");
                    String importance = safeGetText(edgeData, "importance", "");

                    if (!participationType.isEmpty()) {

                        summary.append("- ").append(sourceName).append(" → ").append(targetName)
                               .append("：").append(getParticipationTypeText(participationType));
                        if (!importance.isEmpty()) {
                            summary.append("（重要性：").append(getImportanceText(importance)).append("）");
                        }
                    } else {

                        summary.append("- ").append(sourceName).append(" ↔ ").append(targetName)
                               .append("：").append(relationship);
                    }

                    if (!description.isEmpty()) {
                        summary.append("，").append(description);
                    }
                    summary.append("\n");
                } else {
                    summary.append("- ").append(sourceName).append(" ↔ ").append(targetName)
                           .append("：").append(relationship);
                    if (!description.isEmpty()) {
                        summary.append("，").append(description);
                    }
                    summary.append("\n");
                }
            }
            summary.append("\n");
        }

        // 处理线索关系
        JsonNode clueEdges = edges.get("clue");
        if (clueEdges != null && clueEdges.isArray() && clueEdges.size() > 0) {
            hasRelationships = true;
            summary.append("线索关系（").append(clueEdges.size()).append("个）：\n");
            for (JsonNode edge : clueEdges) {
                String sourceName = nodeIdToName.getOrDefault(safeGetText(edge, "source", ""), "未知线索");
                String targetName = nodeIdToName.getOrDefault(safeGetText(edge, "target", ""), "未知线索");
                String relationship = safeGetText(edge, "relationship", "关联");
                String description = safeGetText(edge, "description", "");

                summary.append("- ").append(sourceName).append(" ↔ ").append(targetName)
                       .append("：").append(relationship);
                if (!description.isEmpty()) {
                    summary.append("，").append(description);
                }
                summary.append("\n");
            }
            summary.append("\n");
        }

        // 处理氛围关系
        JsonNode atmosphereEdges = edges.get("atmosphere");
        if (atmosphereEdges != null && atmosphereEdges.isArray() && atmosphereEdges.size() > 0) {
            hasRelationships = true;
            summary.append("氛围关系（").append(atmosphereEdges.size()).append("个）：\n");
            for (JsonNode edge : atmosphereEdges) {
                String sourceName = nodeIdToName.getOrDefault(safeGetText(edge, "source", ""), "未知节点");
                String targetName = nodeIdToName.getOrDefault(safeGetText(edge, "target", ""), "未知节点");
                String relationship = safeGetText(edge, "relationship", "影响");
                String description = safeGetText(edge, "description", "");

                summary.append("- ").append(sourceName).append(" → ").append(targetName)
                       .append("：").append(relationship);
                if (!description.isEmpty()) {
                    summary.append("，").append(description);
                }
                summary.append("\n");
            }
            summary.append("\n");
        }

        if (!hasRelationships) {
            summary.append("暂无节点关系连接，各设计元素相对独立。\n\n");
        }
    }
    


    
    /**
     * 添加完整的讨论历史
     */
    private void addCompleteDiscussion(StringBuilder summary, JsonNode contextData) {
        JsonNode chat = contextData.get("chat");
        if (chat != null && chat.isArray() && chat.size() > 0) {
            summary.append("【团队讨论历史】\n");
            summary.append("完整的协作讨论过程（共").append(chat.size()).append("条消息）：\n");

            for (int i = 0; i < chat.size(); i++) {
                JsonNode message = chat.get(i);
                if (message != null) {
                    String user = message.get("user").asText("用户");
                    String text = message.get("text").asText("");
                    boolean isAI = message.get("isAI").asBoolean(false);
                    String time = message.get("time").asText("");

                    if (!text.trim().isEmpty()) {
                        summary.append((i + 1)).append(". ");
                        if (!time.isEmpty()) {
                            summary.append("[").append(time).append("] ");
                        }
                        summary.append(isAI ? "AI助手" : user)
                               .append("：").append(text).append("\n");
                    }
                }
            }
            summary.append("\n");
        } else {
            summary.append("【团队讨论历史】\n");
            summary.append("暂无讨论记录，这是协作的开始阶段。\n\n");
        }
    }

    /**
     * 创建节点ID到节点名称的映射
     */
    private Map<String, String> createNodeIdMapping(JsonNode nodes) {
        Map<String, String> nodeIdToName = new HashMap<>();

        // 遍历所有类型的节点
        if (nodes.isObject()) {
            nodes.fields().forEachRemaining(entry -> {
                String nodeType = entry.getKey();
                JsonNode nodeArray = entry.getValue();

                if (nodeArray.isArray()) {
                    for (JsonNode node : nodeArray) {
                        String nodeId = node.get("id").asText("");
                        String nodeName = "";

                        JsonNode data = node.get("data");
                        if (data != null) {
                            // 根据节点类型获取合适的名称字段
                            switch (nodeType) {
                                case "character":
                                case "person":
                                    nodeName = data.get("name").asText("");
                                    break;
                                case "scene":
                                    // 场景节点优先使用title，如果没有则使用sceneDescription的前20个字符
                                    nodeName = data.get("title").asText("");
                                    if (nodeName.isEmpty()) {
                                        String sceneDesc = data.get("sceneDescription").asText("");
                                        nodeName = sceneDesc.length() > 20 ? sceneDesc.substring(0, 20) + "..." : sceneDesc;
                                    }
                                    break;
                                case "clue":
                                case "inference":
                                case "atmosphere":
                                    nodeName = data.get("title").asText("");
                                    break;
                                default:
                                    // 对于未知类型，尝试多个可能的名称字段
                                    nodeName = data.get("title").asText(
                                        data.get("name").asText(
                                            data.get("label").asText("")
                                        )
                                    );
                            }
                        }

                        if (nodeName.isEmpty()) {
                            nodeName = node.get("title").asText("未命名" + nodeType);
                        }

                        if (!nodeId.isEmpty()) {
                            nodeIdToName.put(nodeId, nodeName);
                        }
                    }
                }
            });
        }

        return nodeIdToName;
    }

    /**
     * 获取关系类型的中文描述
     */
    private String getRelationshipTypeText(String type) {
        if (type == null || type.isEmpty()) return "关系";

        switch (type) {
            case "friend": return "朋友";
            case "lover": return "恋人";
            case "family": return "家人";
            case "colleague": return "同事";
            case "mentor": return "师生";
            case "enemy": return "敌人";
            case "rival": return "竞争";
            case "partner": return "合作";
            case "superior": return "上下级";
            case "other": return "其他";
            default: return type;
        }
    }

    /**
     * 获取关系状态的中文描述
     */
    private String getStatusText(String status) {
        if (status == null || status.isEmpty()) return "";

        switch (status) {
            case "active": return "活跃";
            case "passive": return "潜在";
            case "conflict": return "冲突";
            case "stable": return "稳定";
            case "tense": return "紧张";
            default: return status;
        }
    }

    /**
     * 获取参与类型的中文描述
     */
    private String getParticipationTypeText(String type) {
        if (type == null || type.isEmpty()) return "参与";

        switch (type) {
            case "protagonist": return "主角参与";
            case "supporting": return "配角参与";
            case "antagonist": return "反派参与";
            case "witness": return "见证者";
            case "victim": return "受害者";
            case "helper": return "帮助者";
            case "obstacle": return "阻碍者";
            case "other": return "其他参与";
            default: return type + "参与";
        }
    }

    /**
     * 获取重要性的中文描述
     */
    private String getImportanceText(String importance) {
        if (importance == null || importance.isEmpty()) return "";

        switch (importance) {
            case "critical": return "关键";
            case "high": return "高";
            case "normal": return "普通";
            case "low": return "低";
            default: return importance;
        }
    }

    /**
     * 安全获取字符串字段
     */
    private String safeGetText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null) return defaultValue;
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText(defaultValue) : defaultValue;
    }

    /**
     * 安全获取数组字段并转换为字符串
     */
    private String safeGetArrayAsText(JsonNode node, String fieldName) {
        if (node == null) return "";
        JsonNode field = node.get(fieldName);
        if (field == null || !field.isArray()) return "";

        List<String> items = new ArrayList<>();
        for (JsonNode item : field) {
            if (item != null) {
                items.add(item.asText());
            }
        }
        return String.join("、", items);
    }

    /**
     * 检查字段是否为非空数组
     */
    private boolean isNonEmptyArray(JsonNode node, String fieldName) {
        if (node == null) return false;
        JsonNode field = node.get(fieldName);
        return field != null && field.isArray() && field.size() > 0;
    }
}
