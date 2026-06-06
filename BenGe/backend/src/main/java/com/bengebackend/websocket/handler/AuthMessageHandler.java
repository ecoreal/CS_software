package com.bengebackend.websocket.handler;

import com.bengebackend.websocket.auth.WebSocketAuthService;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.session.RoomManager;
import com.bengebackend.websocket.session.SessionRegistry;
import com.bengebackend.websocket.util.WebSocketMessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.logging.Logger;

/**
 * 认证消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMessageHandler implements MessageHandler {
    
    private final WebSocketAuthService authService;
    private final SessionRegistry sessionRegistry;
    private final RoomManager roomManager;
    private final WebSocketMessageUtil messageUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String getSupportedMessageType() {
        return "auth";
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage message) {
        String token = message.getToken();
        String roomId = message.getRoomId();
        String avatar = message.getAvatar();
        
        // 验证必要参数
        if (token == null || token.isEmpty()) {
            log.warn("认证消息缺少token: sessionId={}", session.getId());
            messageUtil.sendErrorMessage(session, "Missing token");
            closeSession(session, "Missing token");
            return;
        }
        
        if (roomId == null || roomId.isEmpty()) {
            log.warn("认证消息缺少roomId: sessionId={}", session.getId());
            messageUtil.sendErrorMessage(session, "Missing roomId");
            closeSession(session, "Missing roomId");
            return;
        }
        
        // 执行认证和授权
        WebSocketAuthService.AuthResult authResult = authService.authenticateAndAuthorize(token, roomId);
        
        if (!authResult.isSuccess()) {
            log.warn("认证失败: sessionId={}, error={}", session.getId(), authResult.getErrorMessage());
            messageUtil.sendErrorMessage(session, authResult.getErrorMessage());
            closeSession(session, "Authentication failed");
            return;
        }
        
        // 认证成功，注册会话
        sessionRegistry.registerUserSession(session.getId(), authResult.getUser().getId());
        sessionRegistry.registerRoomSession(session.getId(), roomId);
        sessionRegistry.registerAvatarSession(session.getId(),avatar);
        
        // 加入房间
        roomManager.joinRoom(roomId, session);

        log.info("用户认证成功并加入房间: userId={}, username={}, roomId={}, sessionId={},avatar={}",
                authResult.getUser().getId(), authResult.getUser().getUsername(), roomId, session.getId(),sessionRegistry.getAvatar(session.getId()));
        
        // 发送用户信息
        messageUtil.sendUserInfo(session, authResult.getUser());
        
        // 广播用户加入消息
        roomManager.broadcastSystemMessage(roomId, authResult.getUser().getUsername() + " 加入了房间");
        
        // 广播房间成员列表
        messageUtil.broadcastRoomMembers(roomId, roomManager, sessionRegistry);
    }
    
    private void closeSession(WebSocketSession session, String reason) {
        try {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason(reason));
        } catch (Exception e) {
            log.error("关闭会话失败: sessionId={}", session.getId(), e);
        }
    }
}
