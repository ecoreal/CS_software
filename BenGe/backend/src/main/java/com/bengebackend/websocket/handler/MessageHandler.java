package com.bengebackend.websocket.handler;

import com.bengebackend.websocket.message.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 消息处理器接口
 */
public interface MessageHandler {
    
    /**
     * 获取支持的消息类型
     */
    String getSupportedMessageType();
    
    /**
     * 处理消息
     * @param session WebSocket会话
     * @param message 消息对象
     */
    void handleMessage(WebSocketSession session, WebSocketMessage message);
    
    /**
     * 是否支持该消息类型
     */
    default boolean supports(String messageType) {
        return getSupportedMessageType().equals(messageType);
    }
}
