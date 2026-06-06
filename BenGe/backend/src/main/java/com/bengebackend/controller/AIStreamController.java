package com.bengebackend.controller;

import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.entity.Slogan;
import com.bengebackend.entity.AIMsgDevide;
import com.bengebackend.model.ScriptHistory;
import com.bengebackend.service.AIService;
import com.bengebackend.service.ScriptHistoryService;
import com.bengebackend.util.ContextDataProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.bengebackend.service.ScriptService;
import com.bengebackend.dto.ScriptDetailDto;
import com.bengebackend.entity.ScriptReplyRequestEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("api/ai")
@Slf4j
public class AIStreamController {

    @Autowired
    private AIService aiService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ScriptHistoryService scriptHistoryService;

    private final ExecutorService executor = new ThreadPoolExecutor(
            2, // corePoolSize
            10, // maximumPoolSize
            60L, // keepAliveTime
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), // 有界队列，最多缓存100个任务
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy() // 拒绝策略：抛出异常
    );
    @Autowired
    private ContextDataProcessor contextDataProcessor;

    /**
     * Slogan流式生成接口
     */
    @PostMapping(value = "/slogan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateSloganStream(@RequestBody SloganRequestEntity request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                aiService.GenerateSloganStreamAsync(request, slogan -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("slogan")
                                .data(slogan));
                    } catch (IOException e) {
                        System.err.println("发送Slogan事件失败: " + e.getMessage());
                        emitter.completeWithError(e);
                    }
                }).get(); // 等待完成

                emitter.complete();
            } catch (Exception e) {
                System.err.println("生成Slogan流式输出失败: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 非流式Slogan生成接口 - 返回三个Slogan对象的数组
     */
    @PostMapping("/slogan/generate")
    public ResponseEntity<List<Slogan>> generateSlogan(@RequestBody SloganRequestEntity request) {
        try {
            // 调用AI服务生成Slogan
            CompletableFuture<List<Slogan>> future = aiService.GenerateSloganAsync(request);
            List<Slogan> slogans = future.get();

            return ResponseEntity.ok(slogans);
        } catch (Exception e) {
            log.error("生成Slogan失败", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * AI助手流式聊天接口
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> messages = (List<Map<String, String>>) request.get("message");

                if (messages == null) {
                    emitter.completeWithError(new IllegalArgumentException("message参数不能为空"));
                    return;
                }

                aiService.ChatStreamAsync(messages, content -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(content));
                    } catch (IOException e) {
                        System.err.println("发送聊天事件失败: " + e.getMessage());
                        emitter.completeWithError(e);
                    }
                }).get(); // 等待完成

                emitter.complete();
            } catch (Exception e) {
                System.err.println("AI助手流式聊天失败: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * GenFramework流式输出接口
     */
    @PostMapping(value = "/genframework/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateFrameworkStream(@RequestBody ScriptReplyRequestEntity request) {
        SseEmitter emitter = new SseEmitter(0L);
        // 准备消息
        ScriptDetailDto sdd = scriptService.getScriptByIdAsync(request.getScriptId());
        List<Map<String, String>> messages = new ArrayList<>();
        if (sdd != null) {
            List<ScriptHistory> history = sdd.getHistory();
            for (ScriptHistory h : history) {
                Map<String, String> msg = new HashMap<>();
                if (h.getResponse() == "" && h.getMessage() != "") {
                    msg.put("role", "user");
                    msg.put("content", h.getMessage());
                } else {
                    msg.put("role", "assistant");
                    msg.put("content", h.getResponse());
                }
                messages.add(msg);
            }
        }
        messages.add(new HashMap<String, String>() {
            {
                put("role", "user");
                put("content", request.getMessage());
            }
        });

        // 启动异步流式处理
        CompletableFuture<AIMsgDevide> future = aiService.GenFrameworkStream(messages, chunk -> {

            try {
                // 发送数据块到客户端
                emitter.send(SseEmitter.event()
                        .name("message") // 事件名称
                        .data(chunk) // 实际内容
                        .reconnectTime(3000));// 重连时间
            } catch (IOException e) {
                // 发送失败时取消任务
                throw new RuntimeException("SSE发送失败", e);
            }
        });

        // 处理完成/异常情况
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // 异常处理
                emitter.completeWithError(ex);
            } else {
                // 正常完成，更新剧本
                emitter.complete();
                scriptService.updateScriptAsync(request.getScriptId(), result.getTitle(), result.getStrScript(), 2);
                // 更新对话历史
                ScriptHistory userHistory = new ScriptHistory();
                userHistory.setScriptId(request.getScriptId());
                userHistory.setMessage(request.getMessage());
                userHistory.setResponse("");
                userHistory.setCreatedAt(LocalDateTime.now());
                scriptHistoryService.addHistory(userHistory);

                ScriptHistory aiHistory = new ScriptHistory();
                aiHistory.setScriptId(request.getScriptId());
                aiHistory.setMessage("");
                aiHistory.setResponse(result.getMsgForUser());
                aiHistory.setCreatedAt(LocalDateTime.now());
                scriptHistoryService.addHistory(aiHistory);
            }
        });

        // 处理连接关闭
        emitter.onCompletion(() -> {
            if (!future.isDone()) {
                future.cancel(true); // 取消仍在进行的任务
            }
        });

        emitter.onTimeout(() -> {
            future.cancel(true); // 超时取消任务
        });

        return emitter;
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AI流式服务运行正常");
    }

}
