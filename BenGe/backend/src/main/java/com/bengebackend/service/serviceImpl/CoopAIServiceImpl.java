package com.bengebackend.service.serviceImpl;

import com.bengebackend.config.XfyunConfig;
import com.bengebackend.service.CoopAIService;
import com.bengebackend.util.ContextDataProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class CoopAIServiceImpl implements CoopAIService{

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XfyunConfig xfyunConfig;

    @Autowired
    private ContextDataProcessor contextDataProcessor;

    public CoopAIServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, XfyunConfig xfyunConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.xfyunConfig = xfyunConfig;
    }

    @Override
    public String chat(String cleanContent, String context, String userName) {

        String message = xfyunConfig.buildEnhancedPrompt(cleanContent, context, userName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "x1");
        requestBody.put("max_tokens", 2048);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", XfyunConfig.SYSTEM_MESSAGE),
                Map.of("role", "user", "content", message)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xfyunConfig.getApiPassword());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("发送消息给星火 X1：{}", message);

            ResponseEntity<Map> response = restTemplate.exchange(
                    xfyunConfig.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<?, ?> result = response.getBody();
            if (result == null || !result.containsKey("choices")) {
                log.error("AI 响应无效：{}", result);
                return "🤖 哎呀，我好像卡壳了，再试一次？";
            }

            Map<?, ?> choice = (Map<?, ?>) ((List<?>) result.get("choices")).get(0);
            Map<?, ?> msg = (Map<?, ?>) choice.get("message");
            String content = (String) msg.get("content");

            // 去除 Markdown 格式（如有）
            String cleaned = content
                    .replaceAll("^```(json)?", "")
                    .replaceAll("```$", "")
                    .trim();

            log.info("AI 回复内容：\n{}", cleaned);

            return cleaned;

        } catch (Exception e) {
            log.error("调用讯飞星火 X1 接口失败", e);
            return "抱歉，AI服务暂时不可用，请稍后再试 🙏";
        }
    }

    @Override
    public List<Map<String, String>> getCoopDirection(List<String> keywords) {
        String prompt = String.format("""
                请根据以下关键词整合出6个剧本方向，每个方向需要包含标题（title）和描述（description）。关键词如下：

                %s

                请用如下格式返回：
                [
                  { "title": "xxx", "description": "..." },
                  ...
                ]
                """, objectMapper.valueToTree(keywords).toPrettyString());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "x1");
        requestBody.put("max_tokens", 2048);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "你是一名专业的剧本创作助手，擅长生成剧本方向设计"),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xfyunConfig.getApiPassword());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    xfyunConfig.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<?, ?> body = response.getBody();
            if (body == null || !body.containsKey("choices")) {
                log.error("AI 响应无效：{}", body);
                return List.of();
            }

            Map<?, ?> choice = (Map<?, ?>) ((List<?>) body.get("choices")).get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            String reply = (String) message.get("content");

            log.info("AI 原始回复内容：\n{}", reply);

            // 清理 markdown 格式
            String cleanedReply = reply
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            log.info("AI 清洗后的内容：\n{}", cleanedReply);

            return objectMapper.readValue(cleanedReply, new TypeReference<List<Map<String, String>>>(){
            });
        } catch (Exception e) {
            log.error("调用讯飞星火 X1 接口失败", e);
            return List.of();
        }
    }

    @Override
    public String getCompleteScript(String contextData) {

        String contextSummary = contextDataProcessor.generateContextSummary(contextData, "writer");

        String userPrompt = XfyunConfig.buildCollaborationPrompt(contextSummary);

        try {
            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + xfyunConfig.getApiPassword());

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "x1");
            requestBody.put("user", UUID.randomUUID().toString());
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "user",
                    "content", userPrompt
            ));
            requestBody.put("messages", messages);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    xfyunConfig.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }
            }

            log.warn("星火模型返回内容无法解析: {}", response.getBody());
            return "模型未返回有效内容";

        } catch (Exception e) {
            log.error("星火模型调用失败", e);
            return "调用失败: " + e.getMessage();
        }
    }

    public String generateNodesStr(String userInput, String designerType, Integer count, String contextData) {
        contextData=contextDataProcessor.generateContextSummary(contextData, designerType);
        String message = xfyunConfig.buildPrompt(userInput, designerType, count, contextData);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "x1");
        requestBody.put("max_tokens", 2048);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", XfyunConfig.SYSTEM_MESSAGE),
                Map.of("role", "user", "content", message)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xfyunConfig.getApiPassword());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("发送消息给星火 X1：{}", message);

            ResponseEntity<Map> response = restTemplate.exchange(
                    xfyunConfig.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<?, ?> result = response.getBody();
            if (result == null || !result.containsKey("choices")) {
                log.error("AI 响应无效：{}", result);
                return "🤖 哎呀，我好像卡壳了，再试一次？";
            }

            Map<?, ?> choice = (Map<?, ?>) ((List<?>) result.get("choices")).get(0);
            Map<?, ?> msg = (Map<?, ?>) choice.get("message");
            String content = (String) msg.get("content");

            // 去除 Markdown 格式（如有）
            String cleaned = content
                    .replaceAll("^```(json)?", "")
                    .replaceAll("```$", "")
                    .trim();

            log.info("AI 回复内容：\n{}", cleaned);

            return cleaned;

        } catch (Exception e) {
            log.error("调用讯飞星火 X1 接口失败", e);
            return "抱歉，AI服务暂时不可用，请稍后再试 🙏";
        }
    }

    @Override
    public List<Map<String, Object>> generateNodes(String userInput, String designerType, Integer count, String contextData){
        String response = generateNodesStr(userInput, designerType, count, contextData);
        log.info("AI原始回复: {}", response);

        return xfyunConfig.parseAiResponse(response, designerType);
    }
}
