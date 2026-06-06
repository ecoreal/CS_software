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

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleMessageProcessor {
    private final RoomManager roomManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void processRoleMessage(WebSocketSession session, String roomId, Integer userId, User user, String roleName) {
        log.info("Received role message from user {} in room {} select role {}", userId, roomId, roleName);

        // 构建系统消息
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("role");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setRoleName(roleName);

            String jsonMessage = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, jsonMessage);
            log.info("Broadcasted role message to room {}", roomId);
        } catch (Exception e){
            log.error("Failed to broadcast role message to room {}", roomId, e);
            broadcastRoleErrorMessage(roomId, userId, user, "角色选择失败");
        }
    }

    private void broadcastRoleErrorMessage(String roomId, Integer userId, User user, String errorMessage) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("roleError");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setContent(errorMessage);

            String jsonMessage = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, jsonMessage);
            log.info("Broadcasted role error message to room {}", roomId);
        }  catch (Exception e){
            log.error("Failed to broadcast role error message to room {}", roomId, e);
        }
    }

    private String getCurrentFormattedTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
