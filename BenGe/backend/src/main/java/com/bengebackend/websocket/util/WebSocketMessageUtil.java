package com.bengebackend.websocket.util;

import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.websocket.session.RoomManager;
import com.bengebackend.websocket.session.SessionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket消息工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageUtil {
    
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送错误消息
     */
    public void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            JsonNode errorMsg = objectMapper.createObjectNode()
                    .put("type", "error")
                    .put("message", errorMessage);
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
        } catch (Exception e) {
            log.error("发送错误消息失败: sessionId={}", session.getId(), e);
        }
    }
    
    /**
     * 发送用户信息
     */
    public void sendUserInfo(WebSocketSession session, User user) {
        try {
            JsonNode userInfo = objectMapper.createObjectNode()
                    .put("type", "userInfo")
                    .put("userId", user.getId())
                    .put("username", user.getUsername());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(userInfo)));
        } catch (Exception e) {
            log.error("发送用户信息失败: sessionId={}", session.getId(), e);
        }
    }
    
    /**
     * 广播房间成员列表
     */
    public void broadcastRoomMembers(String roomId, RoomManager roomManager, SessionRegistry sessionRegistry) {
        try {
            CopyOnWriteArraySet<WebSocketSession> clients = roomManager.getRoomSessions(roomId);
            if (clients == null) return;
            
            // 获取房间内所有用户信息
            List<Map<String, Object>> members = new ArrayList<>();
            for (WebSocketSession client : clients) {
                Optional<Integer> userIdOpt = sessionRegistry.getUserId(client.getId());
                if (userIdOpt.isPresent()) {
                    Optional<User> userOpt = userMapper.findById(userIdOpt.get());
                    log.info("草拟吗={}", sessionRegistry.getAvatar(client.getId()));
                    if (userOpt.isPresent()) {
                        Map<String, Object> member = new HashMap<>();
                        member.put("id", userIdOpt.get());
                        member.put("username", userOpt.get().getUsername());
                        member.put("avatar", sessionRegistry.getAvatar(client.getId()).get()); // 可以后续添加头像URL
                        members.add(member);
                    }
                }
            }
            
            // 创建成员列表消息
            JsonNode membersMsg = objectMapper.createObjectNode()
                    .put("type", "members")
                    .put("roomId", roomId)
                    .set("members", objectMapper.valueToTree(members));
            
            String payload = objectMapper.writeValueAsString(membersMsg);
            roomManager.broadcastToRoom(roomId, payload);
            
        } catch (Exception e) {
            log.error("广播房间成员失败: roomId={}", roomId, e);
        }
    }
}
