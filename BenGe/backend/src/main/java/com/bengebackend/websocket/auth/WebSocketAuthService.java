package com.bengebackend.websocket.auth;

import com.bengebackend.config.TokenService;
import com.bengebackend.mapper.RoomMapper;
import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.Room;
import com.bengebackend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * WebSocket认证服务
 * 负责用户认证和房间权限验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketAuthService {
    
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;
    
    /**
     * 认证用户并验证房间权限
     */
    public AuthResult authenticateAndAuthorize(String token, String roomIdStr) {
        try {
            // 验证token并获取用户信息
            Integer userId = tokenService.getUserIdFromToken(token);
            Optional<User> userOpt = userMapper.findById(userId);
            
            if (userOpt.isEmpty()) {
                return AuthResult.failure("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 验证房间
            int roomId = Integer.parseInt(roomIdStr);
            Room room = roomMapper.getRoomById(roomId);
            if (room == null) {
                return AuthResult.failure("房间不存在");
            }
            
            // 检查用户是否有权限加入房间
            boolean canJoin = roomMapper.isUserInRoom(roomId, userId);
            if (!canJoin) {
                // 尝试添加用户到房间
                int result = roomMapper.addRoomMember(roomId, userId, 0);
                if (result <= 0) {
                    return AuthResult.failure("无法加入房间");
                }
                // 更新房间成员数量
                roomMapper.updateRoomMemberCount(roomId, 1);
            }
            
            return AuthResult.success(user, room);
            
        } catch (NumberFormatException e) {
            return AuthResult.failure("房间ID格式错误");
        } catch (Exception e) {
            log.error("认证失败", e);
            return AuthResult.failure("认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 认证结果
     */
    public static class AuthResult {
        private final boolean success;
        private final String errorMessage;
        private final User user;
        private final Room room;
        
        private AuthResult(boolean success, String errorMessage, User user, Room room) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.user = user;
            this.room = room;
        }
        
        public static AuthResult success(User user, Room room) {
            return new AuthResult(true, null, user, room);
        }
        
        public static AuthResult failure(String errorMessage) {
            return new AuthResult(false, errorMessage, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public User getUser() { return user; }
        public Room getRoom() { return room; }
    }
}
