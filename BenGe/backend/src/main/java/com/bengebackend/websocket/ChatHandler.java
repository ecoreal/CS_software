package com.bengebackend.websocket;

import com.bengebackend.mapper.RoomMapper;
import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.websocket.router.MessageRouter;
import com.bengebackend.websocket.session.RoomManager;
import com.bengebackend.websocket.session.SessionRegistry;
import com.bengebackend.websocket.util.WebSocketMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Optional;

/**
 * 重构后的WebSocket聊天处理器
 * 职责单一：仅负责WebSocket生命周期管理和消息分发
 *
 * - 低耦合：通过依赖注入和接口实现松耦合
 * - 高内聚：每个组件都有明确的单一职责
 * - 易扩展：支持插件式的消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler implements WebSocketHandler {
    
    private final MessageRouter messageRouter;
    private final SessionRegistry sessionRegistry;
    private final RoomManager roomManager;
    private final WebSocketMessageUtil messageUtil;
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("新的WebSocket连接已建立: sessionId={}, 远程地址={}", 
                session.getId(), session.getRemoteAddress());
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.debug("收到消息: sessionId={}, payload={}", session.getId(), payload);
        
        // 将消息路由到对应的处理器
        messageRouter.routeMessage(session, payload);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 获取会话信息并清理
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.removeSession(session.getId());
        
        if (sessionInfo.getRoomId() != null) {
            // 从房间中移除
            boolean removed = roomManager.leaveRoom(sessionInfo.getRoomId(), session);
            
            if (removed && sessionInfo.getUserId() != null) {
                log.info("用户已从房间断开: userId={}, roomId={}, 状态={}", 
                        sessionInfo.getUserId(), sessionInfo.getRoomId(), closeStatus);
                
                // 广播用户离开消息
                broadcastUserLeaveMessage(sessionInfo);
                
                // 更新数据库中的房间成员数量
                updateRoomMemberCount(sessionInfo.getRoomId(), -1);
                
                // 广播更新后的成员列表
                messageUtil.broadcastRoomMembers(sessionInfo.getRoomId(), roomManager, sessionRegistry);
            }
        } else {
            log.info("未加入房间的用户已断开: userId={}, 状态={}", 
                    sessionInfo.getUserId(), closeStatus);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 广播用户离开消息
     */
    private void broadcastUserLeaveMessage(SessionRegistry.SessionInfo sessionInfo) {
        if (sessionInfo.getUserId() != null) {
            Optional<User> userOpt = userMapper.findById(sessionInfo.getUserId());
            if (userOpt.isPresent()) {
                roomManager.broadcastSystemMessage(sessionInfo.getRoomId(), 
                        userOpt.get().getUsername() + " 离开了房间");
            }
        }
    }
    
    /**
     * 更新房间成员数量
     */
    private void updateRoomMemberCount(String roomId, int increment) {
        try {
            roomMapper.updateRoomMemberCount(Integer.parseInt(roomId), increment);
        } catch (NumberFormatException e) {
            log.error("无法更新房间成员数量，房间ID格式错误: {}", roomId);
        } catch (Exception e) {
            log.error("更新房间成员数量失败: roomId={}", roomId, e);
        }
    }
}
