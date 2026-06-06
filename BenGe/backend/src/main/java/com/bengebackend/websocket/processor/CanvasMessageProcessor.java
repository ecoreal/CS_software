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

/**
 * 剧情设计师画布消息处理器 - 处理节点和边的更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanvasMessageProcessor {

    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;

    /**
     * 处理节点更新消息
     */
    public void processNodesUpdate(WebSocketSession session, String roomId, Integer userId,
                                   User user, List<WebSocketMessage.NodeData> nodes) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("nodes");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setNodes(nodes);

            String messageJson = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, messageJson);

            log.info("用户 {} 在房间 {} 更新了节点数据", user.getUsername(), roomId);
        } catch (Exception e) {
            log.error("处理节点更新失败: roomId={}, userId={}", roomId, userId, e);
            broadcastCanvasErrorMessage(roomId, userId, user, "节点更新失败");
        }
    }

    /**
     * 处理边更新消息
     */
    public void processEdgesUpdate(WebSocketSession session, String roomId, Integer userId,
                                   User user, List<WebSocketMessage.EdgeData> edges) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("edges");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setEdges(edges);

            String messageJson = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, messageJson);

            log.info("用户 {} 在房间 {} 更新了边数据", user.getUsername(), roomId);
        } catch (Exception e) {
            log.error("处理边更新失败: roomId={}, userId={}", roomId, userId, e);
            broadcastCanvasErrorMessage(roomId, userId, user, "边更新失败");
        }
    }

    /**
     * 处理画布全量更新(节点和边同时更新)
     */
    public void processFullCanvasUpdate(WebSocketSession session, String roomId, Integer userId,
                                        User user, List<WebSocketMessage.NodeData> nodes,
                                        List<WebSocketMessage.EdgeData> edges) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("canvas");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setNodes(nodes);
            message.setEdges(edges);

            String messageJson = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, messageJson);

            log.info("用户 {} 在房间 {} 更新了完整画布数据", user.getUsername(), roomId);
        } catch (Exception e) {
            log.error("处理画布全量更新失败: roomId={}, userId={}", roomId, userId, e);
            broadcastCanvasErrorMessage(roomId, userId, user, "画布更新失败");
        }
    }

    /**
     * 广播画布错误消息
     */
    private void broadcastCanvasErrorMessage(String roomId, Integer userId, User user, String errorMsg) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("canvas-error");
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