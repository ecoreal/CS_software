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

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteMessageProcessor {

    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;

    public void processVoteUpdate(WebSocketSession session, String roomId, Integer userId, User user,
                                  List<String> key, boolean hasChosen, List<Integer> vote, boolean hasVoted){
        log.info("=== processVoteUpdate 方法被调用 ===");

        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("vote");
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setUsername(user.getUsername());
            message.setTime(getCurrentFormattedTime());
            message.setKey(key);
            message.setHasChosen(hasChosen);
            message.setVote(vote);
            message.setHasVoted(hasVoted);

            log.info("用户 {} 投票处理开始: {}", userId, System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);
            roomManager.broadcastToRoom(roomId, messageJson);

            log.info("用户 {} 投票处理结束: {}", userId, System.currentTimeMillis());
            log.info("用户 {} 在房间 {} 更新了节点数据", user.getUsername(), roomId);
        } catch (Exception e) {
            log.error("处理节点更新失败: roomId={}, userId={}", roomId, userId, e);
            broadcastCanvasErrorMessage(roomId, userId, user, "节点更新失败");
        }
    }

    /**
     * 广播画布错误消息
     */
    private void broadcastCanvasErrorMessage(String roomId, Integer userId, User user, String errorMsg) {
        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType("atmosphere-error");
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
