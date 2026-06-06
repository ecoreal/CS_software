package com.bengebackend.websocket.processor;

import com.bengebackend.model.User;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.session.RoomManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 人物设计师画布消息处理器 - 处理节点和边的更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CharacterMessageProcessor {

    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;

    /**
     * 处理节点更新消息
     */
    public void processFullCanvasUpdate(WebSocketSession session, String roomId, Integer userId,
                                   User user, List<WebSocketMessage.CharacterNode> nodes,
                                   List<WebSocketMessage.CharacterEdge> edges) {
       try {
           WebSocketMessage message = new WebSocketMessage();
           message.setType("character");
           message.setRoomId(roomId);
           message.setUserId(userId);
           message.setUsername(user.getUsername());
           message.setTime(getCurrentFormattedTime());
           message.setCharacterNodes(nodes);
           message.setCharacterEdges(edges);

           String messageJson = objectMapper.writeValueAsString(message);
           roomManager.broadcastToRoom(roomId, messageJson);
           log.info("用户 {} 在房间 {} 更新了人物设计师画布数据", user.getUsername(), roomId);
       } catch (Exception e) {
           log.error("处理人物设计师画布更新失败: roomId={}, userId={}", roomId, userId, e);
           broadcastCanvasErrorMessage(roomId, userId, user, "人物设计师画布更新失败");
       }
   }

    /**
     * 广播画布错误消息
     */
    private void broadcastCanvasErrorMessage(String roomId, Integer userId, User user, String errorMsg) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("character-error");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setContent(errorMsg);

            roomManager.broadcastToRoom(roomId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("发送画布错误消息失败", e);
        }
    }

    /**
     * 获取当前格式化时间
     */
    private String getCurrentFormattedTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
