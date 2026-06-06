package com.bengebackend.service;

import com.bengebackend.entity.AIMsgDevide;
import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.entity.Slogan;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * AI 服务接口
 */
public interface AIService {

    /**
     * 根据传入的消息生成剧本框架
     */
    CompletableFuture<AIMsgDevide> GenFramework(List<Map<String, String>> msgs);

    /**
     * 根据传入的消息生成剧本框架(流式输出)
     */
    CompletableFuture<AIMsgDevide> GenFrameworkStream(List<Map<String, String>> msgs, Consumer<String> callback);

    /**
     * 传入剧本框架及其标题生成详细剧本
     */
    CompletableFuture<String> GenDetail(String Frame, String title);

    /**
     * 传入剧本，进行剧本分析
     */
    CompletableFuture<String> AnalyzeScriptContent(String StrScript);

    /**
     * 传入剧本，生成人物、道具的外貌描绘和场景描写（List存放3个2维数组，每个2维数组包含名称和描绘部分）
     */
    CompletableFuture<List<List<List<String>>>> GetThreeTypesOfDesc(String strScript);

    /**
     * 根据描述生成图片
     */
    CompletableFuture<String> GenImage(String Description);

    /**
     * 生成Slogan流式输出
     */
    CompletableFuture<Void> GenerateSloganStreamAsync(SloganRequestEntity request, Consumer<String> callback);

    /**
     * 生成Slogan非流式输出，返回三个Slogan对象的数组
     */
    CompletableFuture<List<Slogan>> GenerateSloganAsync(SloganRequestEntity request);

    /**
     * AI助手流式聊天
     */
    CompletableFuture<Void> ChatStreamAsync(List<Map<String, String>> messages, Consumer<String> callback);
}
