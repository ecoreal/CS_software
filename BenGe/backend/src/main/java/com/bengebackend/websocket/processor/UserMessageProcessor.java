package com.bengebackend.websocket.processor;

import com.bengebackend.model.User;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.session.RoomManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * 用户消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageProcessor {
    
    private final RoomManager roomManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理用户消息
     */
    public void processUserMessage(WebSocketSession session, String roomId, Integer userId, User user, WebSocketMessage message) {
        try {
            // 构建增强的消息对象
            JsonNode enhancedMsg = objectMapper.createObjectNode()
                    .put("type", "chat")
                    .put("roomId", roomId)
                    .put("userId", userId)
                    .put("username", user.getUsername())
                    .put("content", message.getContent())
                    .put("time", message.getTime())
                    .put("avatar", message.getAvatar() != null ? message.getAvatar() : "");
            
            String enhancedPayload = objectMapper.writeValueAsString(enhancedMsg);
            
            // 广播消息到房间
            roomManager.broadcastToRoom(roomId, enhancedPayload);
            
            log.info("用户 {} 在房间 {} 发送消息，房间人数: {}", 
                    user.getUsername(), roomId, roomManager.getRoomSize(roomId));
            
        } catch (Exception e) {
            log.error("处理用户消息失败: roomId={}, userId={}", roomId, userId, e);
        }
    }
}
