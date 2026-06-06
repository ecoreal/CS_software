package com.bengebackend.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * WebSocket会话注册表
 * 负责管理会话与用户、房间的映射关系
 */
@Slf4j
@Component
public class SessionRegistry {
    
    /**
     * 会话ID -> 用户ID映射
     */
    private final ConcurrentHashMap<String, Integer> sessionUsers = new ConcurrentHashMap<>();
    
    /**
     * 会话ID -> 房间ID映射
     */
    private final ConcurrentHashMap<String, String> sessionRooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,String> sessionAvatar = new ConcurrentHashMap<>();
    
    /**
     * 注册用户会话
     */
    public void registerUserSession(String sessionId, Integer userId) {
        sessionUsers.put(sessionId, userId);
        log.debug("注册用户会话: sessionId={}, userId={}", sessionId, userId);
    }
    
    /**
     * 注册房间会话
     */
    public void registerRoomSession(String sessionId, String roomId) {
        sessionRooms.put(sessionId, roomId);
        log.debug("注册房间会话: sessionId={}, roomId={}", sessionId, roomId);
    }

    public void registerAvatarSession(String sessionId,String avatar){
        sessionAvatar.put(sessionId, avatar);
        log.debug("注册房间会话: sessionId={}, roomId={}", sessionId, avatar);
    }

    /**
     * 获取会话对应的用户ID
     */
    public Optional<Integer> getUserId(String sessionId) {
        return Optional.ofNullable(sessionUsers.get(sessionId));
    }
    
    /**
     * 获取会话对应的房间ID
     */
    public Optional<String> getRoomId(String sessionId) {
        return Optional.ofNullable(sessionRooms.get(sessionId));
    }

    public Optional<String> getAvatar(String sessionId) {
        return Optional.ofNullable(sessionAvatar.get(sessionId));
    }
    
    /**
     * 移除会话
     */
    public SessionInfo removeSession(String sessionId) {
        Integer userId = sessionUsers.remove(sessionId);
        String roomId = sessionRooms.remove(sessionId);
        String avatar = sessionAvatar.remove(sessionId);
        
        log.debug("移除会话: sessionId={}, userId={}, roomId={}", sessionId, userId, roomId);
        
        return new SessionInfo(userId, roomId,avatar);
    }
    
    /**
     * 检查用户是否已认证
     */
    public boolean isAuthenticated(String sessionId) {
        return sessionUsers.containsKey(sessionId);
    }
    
    /**
     * 会话信息
     */
    public static class SessionInfo {
        private final Integer userId;
        private final String roomId;
        private final String avatar;



        public SessionInfo(Integer userId, String roomId,String avatar) {
            this.userId = userId;
            this.roomId = roomId;
            this.avatar = avatar;
        }
        
        public Integer getUserId() { return userId; }
        public String getRoomId() { return roomId; }
        public String getAvatar() { return avatar; }
    }
}
