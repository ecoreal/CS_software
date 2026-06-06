package com.bengebackend.service;

import java.util.List;
import java.util.Map;

public interface CoopAIService {
    /**
     * 对话区发送消息给AI并获取回复
     * @return AI回复
     */
    String chat(String cleanContent, String contextData, String userName);

    /**
     * 获取协作阶段的方向生成
     */
    List<Map<String, String>> getCoopDirection(List<String> keywords);

    /**
     * 获取整合框架生成的完整剧本内容
     */
    public String getCompleteScript(String userPrompt);

    /**
     * AI生成结点
     */
    public List<Map<String, Object>> generateNodes(String userInput, String designerType, Integer count, String contextData);
}
