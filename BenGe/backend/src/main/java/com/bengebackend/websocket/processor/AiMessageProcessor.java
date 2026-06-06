package com.bengebackend.websocket.processor;

import com.bengebackend.model.User;
import com.bengebackend.service.CoopAIService;
import com.bengebackend.util.ContextDataProcessor;
import com.bengebackend.websocket.session.RoomManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AI消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMessageProcessor {

    private final CoopAIService coopAIService;
    private final RoomManager roomManager;
    private final ContextDataProcessor contextDataProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理AI消息
     */
    public void processAiMessage(WebSocketSession session, String roomId, Integer userId, User user, String content) {
        try {
            String cleanContent;
            String contextData = "";

            // 检测消息格式并正确解析
            if (content.startsWith("@ai:")) {
                // JSON格式：@ai:{"userMessage":"...","contextData":{...}}
                String jsonPart = content.substring(4); // 移除"@ai:"，保留JSON部分
                try {
                    JsonNode messageJson = objectMapper.readTree(jsonPart);
                    cleanContent = messageJson.get("userMessage").asText();
                    JsonNode contextDataNode = messageJson.get("contextData");
                    if (contextDataNode != null) {
                        contextData = objectMapper.writeValueAsString(contextDataNode);
                    }
                } catch (Exception e) {
                    log.error("解析AI消息JSON失败", e);
                    // 解析失败时回退到简单处理
                    cleanContent = content.replaceFirst("^@ai:?\\s*", "").trim();
                }
            } else {
                // 简单格式：@ai 用户消息
                cleanContent = content.replaceFirst("^@ai\\s*", "").trim();
            }

            if (cleanContent.isEmpty()) {
                cleanContent = "你好";
            }
            
            log.info("用户 {} 在房间 {} 向AI发送消息: {}", user.getUsername(), roomId, cleanContent);
            
            // 先广播用户的原始消息
            broadcastUserMessage(roomId, userId, user, "@ai"+cleanContent);
            
            // 调用AI服务获取回复
            String aiResponse = coopAIService.chat(cleanContent, contextData, user.getUsername());

            log.info(aiResponse);

            
            // 广播AI回复
            broadcastAiMessage(roomId, aiResponse);
            
            log.info("AI在房间 {} 回复: {}", roomId, aiResponse);
            
        } catch (Exception e) {
            log.error("处理AI消息失败: roomId={}, userId={}", roomId, userId, e);
            broadcastAiErrorMessage(roomId);
        }
    }

    /**
     * 广播用户消息
     */
    private void broadcastUserMessage(String roomId, Integer userId, User user, String content) {
        try {
            JsonNode userMsg = objectMapper.createObjectNode()
                    .put("type", "chat")
                    .put("roomId", roomId)
                    .put("userId", userId)
                    .put("username", user.getUsername())
                    .put("content", content)
                    .put("time", getCurrentFormattedTime())
                    .put("avatar", "");
            
            roomManager.broadcastToRoom(roomId, objectMapper.writeValueAsString(userMsg));
        } catch (Exception e) {
            log.error("广播用户消息失败", e);
        }
    }
    
    /**
     * 广播AI回复
     */
    private void broadcastAiMessage(String roomId, String aiResponse) {
        try {
            JsonNode aiMsg = objectMapper.createObjectNode()
                    .put("type", "chat")
                    .put("roomId", roomId)
                    .put("userId", -1)
                    .put("username", "AI助手")
                    .put("content", aiResponse)
                    .put("time", getCurrentFormattedTime())
                    .put("avatar", "");
            
            roomManager.broadcastToRoom(roomId, objectMapper.writeValueAsString(aiMsg));
        } catch (Exception e) {
            log.error("广播AI消息失败", e);
        }
    }
    
    /**
     * 广播AI错误消息
     */
    private void broadcastAiErrorMessage(String roomId) {
        try {
            JsonNode errorMsg = objectMapper.createObjectNode()
                    .put("type", "chat")
                    .put("roomId", roomId)
                    .put("userId", -1)
                    .put("username", "AI助手")
                    .put("content", "抱歉，AI服务暂时不可用，请稍后再试。")
                    .put("time", getCurrentFormattedTime())
                    .put("avatar", "");
            
            roomManager.broadcastToRoom(roomId, objectMapper.writeValueAsString(errorMsg));
        } catch (Exception e) {
            log.error("发送AI错误消息失败", e);
        }
    }
    
    /**
     * 获取当前格式化时间
     */
    private String getCurrentFormattedTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
