package com.bengebackend.websocket.handler;

import com.bengebackend.mapper.UserMapper;
import com.bengebackend.model.User;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.processor.ClueMessageProcessor;
import com.bengebackend.websocket.session.SessionRegistry;
import com.bengebackend.websocket.util.WebSocketMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClueMessageHandler implements MessageHandler{

    private final SessionRegistry sessionRegistry;
    private final UserMapper userMapper;
    private final ClueMessageProcessor clueMessageProcessor;
    private final WebSocketMessageUtil messageUtil;

    @Override
    public String getSupportedMessageType() {
        return "clue";
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage message) {
        Optional<Integer> userIdOpt = sessionRegistry.getUserId(session.getId());
        if(userIdOpt.isEmpty()){
            log.warn("未认证的用户尝试发送聊天消息: sessionId={}", session.getId());
            messageUtil.sendErrorMessage(session, "请先进行认证");
            return;
        }

        Optional<String> roomIdOpt = sessionRegistry.getRoomId(session.getId());
        if(roomIdOpt.isEmpty()){
            log.warn("用户未加入任何房间: sessionId={}, userId={}", session.getId(), userIdOpt.get());
            return;
        }

        Integer userId = userIdOpt.get();
        String roomId = roomIdOpt.get();

        Optional<User> userOpt = userMapper.findById(userId);
        if(userOpt.isEmpty()){
            log.error("用户不存在: userId={}", userId);
            return;
        }

        User user = userOpt.get();
        List<WebSocketMessage.ClueNode> nodes = message.getClueNodes();
        List<WebSocketMessage.EdgeData> edges = message.getEdges();
        List<WebSocketMessage.InferenceNode> interNodes = message.getInferenceNodes();
        List<WebSocketMessage.PersonNode> personNodes = message.getPersonNodes();

        clueMessageProcessor.processFullCanvasUpdate(session, roomId, userId, user, nodes, interNodes, personNodes, edges);
    }

}
