package com.bengebackend.websocket.handler;

import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.processor.AiMessageProcessor;
import com.bengebackend.websocket.processor.UserMessageProcessor;
import com.bengebackend.websocket.session.SessionRegistry;
import com.bengebackend.websocket.util.WebSocketMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

/**
 * 聊天消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageHandler implements MessageHandler {
    
    private final SessionRegistry sessionRegistry;
    private final UserMapper userMapper;
    private final AiMessageProcessor aiMessageProcessor;
    private final UserMessageProcessor userMessageProcessor;
    private final WebSocketMessageUtil messageUtil;
    
    @Override
    public String getSupportedMessageType() {
        return "chat";
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage message) {
        // 检查用户是否已认证
        Optional<Integer> userIdOpt = sessionRegistry.getUserId(session.getId());
        if (userIdOpt.isEmpty()) {
            log.warn("未认证的用户尝试发送聊天消息: sessionId={}", session.getId());
            messageUtil.sendErrorMessage(session, "请先进行认证");
            return;
        }
        
        // 获取用户当前所在的房间
        Optional<String> roomIdOpt = sessionRegistry.getRoomId(session.getId());
        if (roomIdOpt.isEmpty()) {
            log.warn("用户未加入任何房间: sessionId={}, userId={}", session.getId(), userIdOpt.get());
            return;
        }
        
        Integer userId = userIdOpt.get();
        String roomId = roomIdOpt.get();
        
        // 获取用户信息
        Optional<User> userOpt = userMapper.findById(userId);
        if (userOpt.isEmpty()) {
            log.error("用户不存在: userId={}", userId);
            return;
        }
        
        User user = userOpt.get();
        String content = message.getContent();
        
        // 检查是否是AI消息
        if (isAiMessage(content)) {
            aiMessageProcessor.processAiMessage(session, roomId, userId, user, content);
        } else {
            userMessageProcessor.processUserMessage(session, roomId, userId, user, message);
        }
    }
    
    /**
     * 判断是否是AI消息
     */
    private boolean isAiMessage(String content) {
        return content != null && (content.startsWith("@ai") || content.toLowerCase().contains("@ai"));
    }
}
