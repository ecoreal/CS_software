package com.bengebackend.websocket.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 房间管理器
 * 负责管理WebSocket房间和消息广播
 */
@Slf4j
@Component
public class RoomManager {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 房间ID -> WebSocket会话集合
     */
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    
    /**
     * 加入房间
     */
    public void joinRoom(String roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("用户加入房间: sessionId={}, roomId={}, 房间人数={}", 
                session.getId(), roomId, rooms.get(roomId).size());
    }
    
    /**
     * 离开房间
     */
    public boolean leaveRoom(String roomId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            boolean removed = roomSessions.remove(session);
            if (removed) {
                log.info("用户离开房间: sessionId={}, roomId={}, 剩余人数={}", 
                        session.getId(), roomId, roomSessions.size());
                
                // 如果房间为空，清理房间
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                    log.info("房间已清空并移除: roomId={}", roomId);
                }
            }
            return removed;
        }
        return false;
    }
    
    /**
     * 向房间广播消息
     */
    public void broadcastToRoom(String roomId, String message) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("向房间 {} 广播消息失败: sessionId={}", roomId, session.getId(), e);
                }
            });
        }
    }
    
    /**
     * 向房间广播系统消息
     */
    public void broadcastSystemMessage(String roomId, String message) {
        try {
            JsonNode systemMsg = objectMapper.createObjectNode()
                    .put("type", "system")
                    .put("roomId", roomId)
                    .put("message", message)
                    .put("time", getCurrentFormattedTime());
            
            broadcastToRoom(roomId, objectMapper.writeValueAsString(systemMsg));
        } catch (Exception e) {
            log.error("创建系统消息失败: roomId={}", roomId, e);
        }
    }
    
    /**
     * 获取房间会话数量
     */
    public int getRoomSize(String roomId) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        return roomSessions != null ? roomSessions.size() : 0;
    }
    
    /**
     * 获取房间内的所有会话
     */
    public CopyOnWriteArraySet<WebSocketSession> getRoomSessions(String roomId) {
        return rooms.get(roomId);
    }
    
    /**
     * 获取当前格式化时间
     */
    private String getCurrentFormattedTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
