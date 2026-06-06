package com.bengebackend.websocket.router;

import com.bengebackend.websocket.handler.MessageHandler;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 消息路由器：负责将不同类型的消息路由到对应的处理器
 */
@Slf4j
@Component
public class MessageRouter {

    private final Map<String, MessageHandler> handlerMap;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageRouter(List<MessageHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        MessageHandler::getSupportedMessageType,
                        Function.identity()
                ));
        log.info("注册消息处理器: {}", handlerMap.keySet());
    }

    /**
     * 路由消息到对应的处理器
     */
    public void routeMessage(WebSocketSession session, String payload) {
        try {
            log.debug("收到消息: sessionId={}, payload={}", session.getId(), payload);

            // 解析消息
            JsonNode jsonNode = objectMapper.readTree(payload);
            String messageType = jsonNode.get("type").asText();

            // 转换为消息对象
            WebSocketMessage message = parseMessage(jsonNode);

            // 查找对应的处理器
            MessageHandler handler = handlerMap.get(messageType);
            if (handler != null) {
                handler.handleMessage(session, message);
            } else {
                log.warn("未找到消息类型 {} 的处理器: sessionId={}", messageType, session.getId());
            }

        } catch (Exception e) {
            log.error("路由消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 解析 JSON 消息为 WebSocketMessage 对象
     */
    private WebSocketMessage parseMessage(JsonNode jsonNode) {
        WebSocketMessage message = new WebSocketMessage();

        message.setType(getTextValue(jsonNode, "type"));
        message.setRoomId(getTextValue(jsonNode, "roomId"));
        message.setContent(getTextValue(jsonNode, "content"));
        message.setTime(getTextValue(jsonNode, "time"));
        message.setAvatar(getTextValue(jsonNode, "avatar"));
        message.setToken(getTextValue(jsonNode, "token"));
        message.setUsername(getTextValue(jsonNode, "username"));

        if (has(jsonNode, "userId")) {
            message.setUserId(jsonNode.get("userId").asInt());
        }

        setIfPresent(jsonNode, "roleName", message::setRoleName);
        setBooleanIfPresent(jsonNode, "hasChosen", message::setHasChosen);
        setBooleanIfPresent(jsonNode, "hasVoted", message::setHasVoted);
        setListIfPresent(jsonNode, "key", String.class, message::setKey);
        setListIfPresent(jsonNode, "vote", Integer.class, message::setVote);

        setListIfPresent(jsonNode, "nodes", WebSocketMessage.NodeData.class, message::setNodes);
        setListIfPresent(jsonNode, "edges", WebSocketMessage.EdgeData.class, message::setEdges);
        setListIfPresent(jsonNode, "clueEdges", WebSocketMessage.EdgeData.class, message::setEdges); // 兼容 clueEdges
        setListIfPresent(jsonNode, "characterNodes", WebSocketMessage.CharacterNode.class, message::setCharacterNodes);
        setListIfPresent(jsonNode, "characterEdges", WebSocketMessage.CharacterEdge.class, message::setCharacterEdges);
        setListIfPresent(jsonNode, "clueNodes", WebSocketMessage.ClueNode.class, message::setClueNodes);
        setListIfPresent(jsonNode, "inferenceNodes", WebSocketMessage.InferenceNode.class, message::setInferenceNodes);
        setListIfPresent(jsonNode, "personNodes", WebSocketMessage.PersonNode.class, message::setPersonNodes);
        setListIfPresent(jsonNode, "atmosphereNodes", WebSocketMessage.AtmosphereNode.class, message::setAtmosphereNodes);
        setListIfPresent(jsonNode, "atmosphereEdges", WebSocketMessage.AtmosphereEdge.class, message::setAtmosphereEdges);

        return message;
    }

    /**
     * 安全获取文本值
     */
    private String getTextValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    private boolean has(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull();
    }

    private void setIfPresent(JsonNode node, String field, Consumer<String> setter) {
        if (has(node, field)) {
            setter.accept(node.get(field).asText());
        }
    }

    private void setBooleanIfPresent(JsonNode node, String field, Consumer<Boolean> setter) {
        if (has(node, field)) {
            setter.accept(node.get(field).asBoolean());
        }
    }

    private <T> void setListIfPresent(JsonNode node, String field, Class<T> clazz, Consumer<List<T>> setter) {
        if (has(node, field)) {
            setter.accept(parseList(node.get(field), clazz));
        }
    }

    private <T> List<T> parseList(JsonNode node, Class<T> clazz) {
        try {
            return objectMapper.readValue(
                    node.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            log.error("解析 {} 列表失败", clazz.getSimpleName(), e);
            return null;
        }
    }
}
