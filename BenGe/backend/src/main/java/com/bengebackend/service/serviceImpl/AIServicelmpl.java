package com.bengebackend.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.bengebackend.entity.AIMsgDevide;
import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.entity.Slogan;
import com.bengebackend.service.*;
import com.bengebackend.config.XfyunConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AIServicelmpl implements AIService {

    private final XfyunConfig xfyunConfig;

    @SuppressWarnings("unused")
    private final RestTemplate restTemplate;
    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper;

    public AIServicelmpl(RestTemplate restTemplate, ObjectMapper objectMapper, XfyunConfig xfyunConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.xfyunConfig = xfyunConfig;
    }

    // ====================== 单人创作和多人创作AI部分分割线 ==================

    @Override
    public CompletableFuture<AIMsgDevide> GenFramework(List<Map<String, String>> msgs) {
        msgs.add(0, new HashMap<String, String>() {
            {
                put("role", "system");
                put("content", System_MSG);
            }
        });

        // 获取API输出
        return xfyunConfig.GetAPIOutputAsync(msgs, "x1")
                .thenApply(content -> {
                    AIMsgDevide devidedMsg = new AIMsgDevide();
                    xfyunConfig.DevideScriptContent(devidedMsg, content, true);
                    return devidedMsg;
                });
    }

    @Override
    public CompletableFuture<String> GenDetail(String Frame, String Title) {
        AIMsgDevide devidedMsg = new AIMsgDevide(Title, Frame);
        xfyunConfig.DevideScriptContent(devidedMsg, Frame, false);
        List<List<Map<String, String>>> messagesList = new ArrayList<>(); // 0背景 4-n人物剧本 1线索 2真相 3组织者手册
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", GenDetailSysPrompt[0]));
        messages.add(Map.of(
                "role", "user",
                "content", devidedMsg.getStrScript() + "\n\n" + GenDetailUserPrompt[0]));
        messagesList.add(messages);
        for (int i = 0; i < devidedMsg.getChrScript().size(); i += 1) {
            messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", GenDetailSysPrompt[1]));
            messages.add(Map.of(
                    "role", "user",
                    "content", devidedMsg.getStrScript() + "\n\n" + GenDetailUserPrompt[1] + "\n\n"
                            + devidedMsg.getChrScript().get(i)));
            messagesList.add(messages);
        }
        for (int i = 2; i < 5; i += 1) {
            messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", GenDetailSysPrompt[i]));
            messages.add(Map.of(
                    "role", "user",
                    "content", devidedMsg.getStrScript() + "\n\n" + GenDetailUserPrompt[i]));
            messagesList.add(messages);
        }

        return CompletableFuture.supplyAsync(() -> {
            StringBuilder resultBuilder = new StringBuilder();
            AtomicInteger addTime = new AtomicInteger(0);
            // 分批处理
            for (int i = 0; i < messagesList.size(); i += 2) {
                int end = Math.min(i + 2, messagesList.size());
                List<List<Map<String, String>>> batch = messagesList.subList(i, end);

                // 并行处理当前批次
                List<CompletableFuture<String>> futures = batch.stream()
                        .map(msgs -> xfyunConfig.GetAPIOutputAsync(msgs, "x1"))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // 收集当前批次结果
                futures.forEach(future -> {
                    try {
                        resultBuilder.append(future.join());
                        if (addTime.get() == 0) {
                            resultBuilder.append("\n\n---\n\n## 人物剧本\n\n");
                        }
                        if (addTime.get() != 0 && addTime.get() < devidedMsg.getChrScript().size()) {
                            resultBuilder.append("\n\n");
                        } else {
                            resultBuilder.append("\n\n---\n\n");
                        }
                        addTime.incrementAndGet();
                    } catch (CompletionException e) {
                        resultBuilder.append("[Error: ").append(e.getCause().getMessage()).append("]");
                    }
                });
            }
            return resultBuilder.toString();
        });
    }

    @Override
    public CompletableFuture<String> AnalyzeScriptContent(String StrScript) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", AnalyzePrompt));
        messages.add(Map.of(
                "role", "user",
                "content", StrScript));
        return xfyunConfig.GetAPIOutputAsync(messages, "4.0Ultra");
    }

    @Override
    public CompletableFuture<List<List<List<String>>>> GetThreeTypesOfDesc(String strScript) {
        // 1. 准备三种请求消息
        List<List<Map<String, String>>> messageList = IntStream.range(0, 3)
                .mapToObj(i -> List.of(
                        Map.of("role", "system", "content", GetDescPrompt[i]),
                        Map.of("role", "user", "content", strScript)))
                .collect(Collectors.toList());

        // 2. 分批处理（每批最多2个请求）
        List<CompletableFuture<List<List<String>>>> allFutures = new ArrayList<>();

        for (int i = 0; i < messageList.size(); i += 2) {
            int end = Math.min(i + 2, messageList.size());
            List<List<Map<String, String>>> batch = messageList.subList(i, end);

            // 处理当前批次
            List<CompletableFuture<List<List<String>>>> batchFutures = batch.stream()
                    .map(msgs -> xfyunConfig.ParseAIRespOfGetDesc(xfyunConfig.GetAPIOutputAsync(msgs, "4.0Ultra")))
                    .collect(Collectors.toList());

            // 等待当前批次完成
            CompletableFuture<Void> batchDone = CompletableFuture.allOf(
                    batchFutures.toArray(new CompletableFuture[0]));

            // 添加到总列表
            batchFutures.forEach(allFutures::add);

            // 等待当前批次完成再继续下一批
            batchDone.join();
        }

        // 3. 合并所有结果
        return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> allFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
        // 返回结果示例：
        // [[[a,b,c(角色名)], [aa,bb,cc]], [[d, e, f(场景名)], [dd,ee,ff]], [[g,h,i(道具名)],
        // [gg,hh,ii]]]
    }

    @Override
    public CompletableFuture<String> GenImage(String Description) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestData = new HashMap<>();

                // 构建header
                Map<String, Object> header = new HashMap<>();
                header.put("app_id", xfyunConfig.getAppid_wwb());
                requestData.put("header", header);

                // 构建parameter
                Map<String, Object> parameter = new HashMap<>();
                Map<String, Object> chat = new HashMap<>();
                chat.put("domain", "general");
                chat.put("width", 512);
                chat.put("height", 512);
                parameter.put("chat", chat);
                requestData.put("parameter", parameter);

                // 构建payload
                Map<String, Object> payload = new HashMap<>();
                Map<String, Object> message = new HashMap<>();
                List<Map<String, Object>> textList = new ArrayList<>(); // 使用List而不是Map
                Map<String, Object> textItem = new HashMap<>(); // 创建text数组中的项
                textItem.put("role", "user");
                textItem.put("content", Description);
                textList.add(textItem);
                message.put("text", textList);
                payload.put("message", message);
                requestData.put("payload", payload);

                // 生成带签名的URL
                String requestUrl = xfyunConfig.CreateSignedUrl(xfyunConfig.getGenImageRequestURL());
                System.out.println("请求URL: " + requestUrl);
                // 异步发送请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .header("Content-Type", "application/json")
                        .header("app_id", xfyunConfig.getAppid_wwb())
                        .POST(HttpRequest.BodyPublishers.ofString(new JSONObject(requestData).toString()))
                        .build();

                // 发送异步请求并处理响应
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            String responseBody = response.body();
                            System.out.println("API响应: " + responseBody);

                            if (response.statusCode() != 200) {
                                throw new RuntimeException("HTTP请求失败: " + response.statusCode() + " " + responseBody);
                            }

                            JSONObject resp = new JSONObject(responseBody);

                            return resp
                                    .getJSONObject("payload")
                                    .getJSONObject("choices")
                                    .getJSONArray("text")
                                    .getJSONObject(0)
                                    .getString("content");

                        }).join();
            } catch (Exception e) {
                throw new RuntimeException("创建任务失败: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> GenerateSloganStreamAsync(SloganRequestEntity request, Consumer<String> callback) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 构建请求消息
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", XfyunConfig.SLOGAN_SYSTEM_PROMPT));
                messages.add(Map.of("role", "user", "content", request.getPrompt()));

                // 执行流式请求
                executeStreamRequest(messages, content -> {
                    // 处理内容并生成Slogan对象
                    if (content != null && !content.trim().isEmpty()) {
                        // String coreIdea = extractCoreIdea(content);
                        // Slogan slogan = new Slogan(content.trim(), coreIdea);
                        callback.accept(content);
                    }
                });

            } catch (Exception e) {
                System.err.println("生成Slogan流式输出时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<List<Slogan>> GenerateSloganAsync(SloganRequestEntity request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 构建请求消息
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", XfyunConfig.SLOGAN_SYSTEM_PROMPT));
                messages.add(Map.of("role", "user", "content", request.getPrompt()));

                // 执行非流式请求
                CompletableFuture<String> future = xfyunConfig.GetAPIOutputAsync(messages, "x1");
                String response = future.get();

                // 解析响应内容为Slogan对象数组
                return parseSloganResponse(response);

            } catch (Exception e) {
                System.err.println("生成Slogan时发生错误: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * 解析AI响应内容为Slogan对象列表
     */
    private List<Slogan> parseSloganResponse(String response) {
        List<Slogan> slogans = new ArrayList<>();

        if (response == null || response.trim().isEmpty()) {
            return slogans;
        }

        // 按行分割响应内容
        String[] lines = response.split("\n");
        String currentSlogan = "";
        String currentCoreIdea = "";
        boolean isSlogan = false;
        boolean isCoreIdea = false;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("# 标语")) {
                // 如果之前有完整的标语和核心创意，保存它
                if (!currentSlogan.isEmpty() && !currentCoreIdea.isEmpty()) {
                    slogans.add(new Slogan(currentSlogan, currentCoreIdea));
                }
                // 重置状态
                currentSlogan = "";
                currentCoreIdea = "";
                isSlogan = true;
                isCoreIdea = false;
            } else if (line.startsWith("# 核心创意")) {
                isSlogan = false;
                isCoreIdea = true;
            } else if (!line.isEmpty() && !line.startsWith("#")) {
                // 收集内容
                if (isSlogan) {
                    if (!currentSlogan.isEmpty()) {
                        currentSlogan += "\n" + line;
                    } else {
                        currentSlogan = line;
                    }
                } else if (isCoreIdea) {
                    if (!currentCoreIdea.isEmpty()) {
                        currentCoreIdea += "\n" + line;
                    } else {
                        currentCoreIdea = line;
                    }
                }
            }
        }

        // 保存最后一个标语
        if (!currentSlogan.isEmpty() && !currentCoreIdea.isEmpty()) {
            slogans.add(new Slogan(currentSlogan, currentCoreIdea));
        }

        // 如果解析失败或数量不足，返回默认的3个标语
        if (slogans.size() < 3) {
            while (slogans.size() < 3) {
                slogans.add(new Slogan("精彩剧本等你来体验", "沉浸式角色扮演，解锁真相"));
            }
        }

        // 只返回前3个标语
        return slogans.subList(0, Math.min(3, slogans.size()));
    }

    @Override
    public CompletableFuture<Void> ChatStreamAsync(List<Map<String, String>> messages, Consumer<String> callback) {
        return CompletableFuture.runAsync(() -> {
            try {
                executeStreamRequest(messages, callback);
            } catch (Exception e) {
                System.err.println("AI助手流式聊天时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 执行流式请求的核心方法 - 改进版使用实际的HTTP流式处理
     */
    private void executeStreamRequest(List<Map<String, String>> messages, Consumer<String> callback) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 构建请求体
            String requestBody = mapper.writeValueAsString(Map.of(
                    "model", "x1",
                    "user", "user_id",
                    "messages", messages,
                    "stream", true,
                    "temperature", 0.7,
                    "top_p", 0.9,
                    "presence_penalty", 4.0,
                    "max_tokens", 32768));

            // 使用HttpURLConnection进行流式处理
            java.net.URL url = new java.net.URL(xfyunConfig.getX1HttpApiUrl());
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + xfyunConfig.getX1HttpApiPassword());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);

            // 发送请求数据
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    processStreamLine(line, callback, mapper);
                }
            }

        } catch (Exception e) {
            System.err.println("执行流式请求时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理单行流式响应
     */
    private void processStreamLine(String line, Consumer<String> callback, ObjectMapper mapper) {
        callback.accept(line);
        // if (line.startsWith("data:")) {
        // String jsonData = line.substring(5).trim();

        // // 检查是否是结束标记
        // if ("[DONE]".equals(jsonData)) {
        // return;
        // }

        // try {
        // // 解析JSON响应
        // StreamResponse streamResponse = mapper.readValue(jsonData,
        // StreamResponse.class);

        // // 检查错误码
        // if (streamResponse.getCode() != 0) {
        // System.err.println("API返回错误: " + streamResponse.getMessage());
        // return;
        // }

        // // 提取内容
        // if (streamResponse.getChoices() != null &&
        // !streamResponse.getChoices().isEmpty()) {
        // var choice = streamResponse.getChoices().get(0);
        // if (choice.getDelta() != null) {
        // String content = choice.getDelta().getContent();
        // if (content != null && !content.isEmpty()) {
        // callback.accept(content);
        // }
        // }
        // }

        // } catch (JsonProcessingException e) {
        // System.err.println("解析流式响应JSON时发生错误: " + e.getMessage());
        // }
        // }
    }

    /**
     * 生成框架流式响应
     */
    @Override
    public CompletableFuture<AIMsgDevide> GenFrameworkStream(List<Map<String, String>> msgs,
            Consumer<String> callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                msgs.add(0, new HashMap<String, String>() {
                    {
                        put("role", "system");
                        put("content", System_MSG);
                    }
                });
                // 构建消息数组
                JSONArray messages = new JSONArray();
                for (Map<String, String> msg : msgs) {
                    messages.put(new JSONObject()
                            .put("role", msg.get("role"))
                            .put("content", msg.get("content")));
                }

                // 创建请求连接
                URL url = new URL(xfyunConfig.getX1Url());// X1URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + xfyunConfig.getX1APIPassword());
                conn.setDoOutput(true);

                // 发送请求体
                try (OutputStream os = conn.getOutputStream()) {
                    JSONObject body = new JSONObject()
                            .put("user", "user_id")
                            .put("model", "x1")
                            .put("stream", true)
                            .put("max_tokens", 32768)
                            .put("messages", messages);
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                String StrScript = processStreamResponse(conn, callback);
                AIMsgDevide devidedMsg = new AIMsgDevide();
                xfyunConfig.DevideScriptContent(devidedMsg, StrScript, true);
                return devidedMsg;
            } catch (Exception e) {
                throw new RuntimeException("API请求失败", e);
            }
        });
    }

    private String processStreamResponse(HttpURLConnection conn, Consumer<String> callback) throws Exception {
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("API响应错误: " + responseCode);
        }

        StringBuilder fullContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("[DONE]"))
                    break;
                if (line.isBlank() || line.isEmpty())
                    continue;
                callback.accept(line);
                String block = line;
                try {
                    JSONObject json = new JSONObject(block.substring(5));
                    json = json.getJSONArray("choices").getJSONObject(0).getJSONObject("delta");
                    if (!json.has("content")) {
                        continue;
                    } else {
                        block = json.getString("content");
                        fullContent.append(block);
                    }
                } catch (Exception e) {
                    System.err.print(".");
                }
            }
        }
        return fullContent.toString();
    }

    private static final String[] GenDetailSysPrompt = {
            """
                    你是剧本杀创作的一员，你的任务是完善剧本背景，要求如下：
                    	1、请以如下模板输出
                    	## 背景

                    	...（纯文本）

                    	2、除了”## 背景“这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何Markdown格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                    	3、请根据用户给出的剧本，完善和丰富背景部分的内容，并且只输出背景部分的内容
                                        """,
            """
                    你是一名小说家，你将根据用户提供的剧本杀框架完成用户指定的人物剧本，要求如下：
                                	1、请以 CHR 角色姓名 的形式开头
                                	示例：
                                	CHR 王小明

                                	...（王小明的详细剧本内容）

                                	2、请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                                	3、请根据用户给出的剧本，尽可能完善和丰富用户指定角色的剧本内容，并且只输出用户指定角色的剧本内容

                                	4、请使用线性叙事，严格采用第一人称限知视角，仅通过主角的眼睛观察事件。禁止切换其他角色视角，所有信息必须来自：（1）主角亲眼所见/听闻；（2）主角的回忆；（3）主角对物理证据的推理
                                                                """,
            """
                    你是剧本杀创作的一员，你的任务是完整描述剧本中已有的线索，要求如下：
                    	1、请以如下模板输出
                            	## 线索

                                - C> ...
                    		    - C> ...
                    		    - C> ...

                        2、除了”## 线索“这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                        3、请根据用户给出的剧本，完善和丰富线索部分的内容，并且只输出线索部分的内容

                        4、禁止增加线索数量，请详细描述每一条已有线索
                                        """,
            """
                    你是剧本杀创作的一员，你的任务是完善剧本真相，要求如下：
                        1、请以如下模板输出
                     	## 真相

                        ...（纯文本）

                        2、除了”## 真相“这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                        3、请根据用户给出的剧本，完善和丰富真相部分的内容，并且只输出真相部分的内容
                                        """,
            """
                    你是剧本杀创作的一员，你的任务是完善组织者手册，要求如下：
                    	1、请以 ## 组织者手册\n\n 开头

                    	2、请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                    	3、请根据用户给出的剧本，完善和丰富组织者手册部分的内容，并且只输出组织者手册部分的内容
                                        """
    };

    private static final String[] GenDetailUserPrompt = {
            "\n\n请不遗余力地详细描述上述剧本中的 ## 背景 部分",
            "\n\n请根据上述剧本内容不遗余力地详细描述下述人物的剧本",
            "\n\n请不遗余力地详细描述上述剧本中的 ## 线索 部分",
            "\n\n请不遗余力地详细补充上述剧本中的 ## 真相 部分",
            "\n\n请不遗余力地详细描述上述剧本中的 ## 组织者手册 部分"
    };

    private static final String System_MSG = """
            你是一名逻辑严谨且最求内容完整的剧本杀作家，必须严格遵循以下规则：
                规则1. 请严格按指定MarkDown格式输出以下字段，七部分内容缺一不可！不要包含额外文本！:
            		AI回复（一句话）
            		标题
            		背景（一段话）
            		人物剧本（数组）
            		线索（数组）
            		真相
            		组织者手册

            	输出需符合如下结构：
            		AI回复：
                        ...（这部分是给用户的礼貌性回答）
            		》》》
                # ...（这部分是标题）

            		---

                ## 背景

            		...

            		---

                ## 人物剧本:

            		- CHR ...（角色1名称）
            		...（关于角色1的相关内容）
            		- CHR ...（角色2名称）
            		...（关于角色2的相关内容）
            		...（其它角色的格式以此类推）

            		---

                ## 线索

            		- C> ...
            		- C> ...
            		- C> ...
            		...

            		---

                ## 真相

            		...

            		---

                ## 组织者手册

            		...
            		---
            	（到此结束，不要再输出文本）

                规则2.若有玩家人物是凶手，则需要在其剧本中写明身份；若未写明身份，需在真相处表明原因（如失忆、人格扭曲...）

                规则3. 当用户要求细化某部分时：
                    - 保持原有内容的基础上更新指定部分
                    - 仍然需要输出用户没要求修改的内容
                    - 输出内容必须满足规则1

                规则4. 请按以下内容深化剧本内容：
                    （1）背景：
                        需包含必要的事件背景、人物来历
                        事件的叙述需按时间线描述，要求详细具体
                        必须提供细节描写，以帮助案件推理
                    （2）人物剧本：每个角色必须包含：
                        价值观在其生活中的体现
                        人物背景、人物意图描述
                        时间线（案发前后的行动轨迹）
                        信息差（角色间掌握的信息不尽相同）
                        与其他角色的关系（可以是表面也可以有不为人知的一面）
                        不能出现“其余角色略”的字样
                        每个人物的剧本必须以- CHR 开头（遵循规则1）
                    （3）线索：
                        关键性线索必须有明确的指向
                        每条线索需要有足够多的细节
                        线索数量不能太少
                        每条线索必须以- C> 开头（遵循规则1）
                    （4）真相：
                        必须包含凶手作案过程
                        详细描述每个细节
                    （5）组织者手册：
                        必须足够详细到能够帮助玩家破局，不能过于简单

            	    规则5. 请务必输出每部分之间的分隔符“---”！

                        下面是针对上述规则的示例：
                            人物剧本参考示例（1）：
                        ##人物剧本
            		甲是一名...，甲...（体现价值观），甲的出身...，甲是为了...来到这里，19：00 甲做了...；20：15 甲在...；21：00 甲...；...。甲知道...的事。甲和...有...的关系，和...有...的关系...
            		...（其它角色剧本）

                违规示例（2）：
                    用户要求细化组织者手册时，若仅返回
                      ##组织者手册
            		...
            	      ---
                         → 视为违规
                正确示例（2）：
                    用户要求细化手册时，应满足规则1
                        好的，已细化手册部分，其他部分保持现有内容...
            		---
            		#我的剧本杀标题
                            ---
            		##背景
            		...
            		---
            		##人物剧本:
            		-...
            		-...
            		...
            		---
                            ##线索
            		-...
            		-...
            		...
            		---
                            ##真相
            		...
            		---
            		##组织者手册": "新细化内容..."

                违规示例（3）：
                    输出“维持原有详细内容”及其相近意义的语段视为违规

                示例结束，现在请根据用户请求进行处理：

                                    """;

    private static final String[] GetDescPrompt = { """
            你擅长于提取和完善剧本中所有人物的外貌描写。请遵循以下规则：
                1、严格使用JSON格式，包含name（角色名称）、description（角色外貌描绘）两个字段，请按如下JSON格式输出，不要包含额外文本：
            	    {
                    "name":["...","...","...",...]
                    "description":[
                        "...",
                        "...",
                        "...",
                        ...
                    ]}

                2、若原文有明确人物外貌描写，则提取人物外貌特征到描绘中，若原文描绘不完整，则适当补充。

                3、若原文无人物外貌描写，则生成符合人物身份（年龄/职业/性格）的合理特征到描绘中

                4、若输出与外貌描绘无关的信息，视为违规（例如输出人物关系、背景、原因等信息，是违规的）

                5、请输出剧本提及的所有人物及其外貌描绘

                6、输出示例：
                    {
                    "name":["小明","小红","小王",...]
                    "description":[
                        "...",
                        "...",
                        "...",
                        ...
                    ]}

            请根据用户提供的剧本，以JSON格式输出所有人物的外貌描绘
                                    """,
            """
                    你擅长于提取剧本中场景的环境描写。请遵循以下规则：
                    	1、严格使用JSON格式，包含name（场景名称）、description（场景描绘）两个字段，请按下面的JSON格式输出，不要包含额外文本：
                            {
                            "name":["...","...","...",...]
                            "description":[
                            "...",
                            "...",
                            "...",
                            ...
                            ]}

                        2、若原文有明确场景描写，则提取场景特征到描绘中。请输出剧本提及的所有场景及其环境描写。

                    	3、若输出与场景描绘无关的信息，则视为违规（例如，场景描绘中不能出现人物名称以及人物动作等与人物相关的描述）

                    	4、输出示例：
                            {
                            "name":["浴室","悬崖","厨房",...]
                            "description":[
                            "（浴室的场景描绘）",
                            "（悬崖的场景描绘）",
                            "（厨房的场景描绘）",
                            ...
                            ]}

                    	5、错误示例（1）：
                            {
                            "名称":["浴室","悬崖","厨房"]
                            "描绘":[
                            "浴室的场景描绘...",
                            "悬崖的场景描绘...",
                            ]}
                            解释：名称与描绘必须一一对应，示例中输出3个名称却只有2个描绘

                            错误示例（2）：
                            {
                            "名称":["浴室","悬崖","厨房"]
                            "描绘":[
                            "浴室的场景描绘...",
                            "悬崖连接着...李萌依靠在栏杆上...",
                            "厨房的场景描绘..."
                            ]}
                            解释：悬崖的场景描绘出现人物李萌（或者描述成玩家、众人等对人的抽象），这是违规的

                    请根据用户提供的剧本，输出JSON格式的场景描绘
                                """,
            """
                                    你擅长于提取剧本中道具的静态外观描写。请遵循以下规则：
                            1、严格使用JSON格式，包含name（道具名称）、description（道具描绘）两个字段，请按下面的JSON格式输出，不要包含额外文本：
                                            {
                                            "name":["...","...","...",...]
                                            "description":[
                                            "...",
                                            "...",
                                            "...",
                                            ...
                                            ]}

                                    2、若原文有道具明确的状物描写，则将其提取到描绘中；若无原文描写，则基于物品类型推测其外观

                                    3、描绘字段只能包含物品的视觉特征，例如（不需要全部包含）：
                                       - 颜色、形状、材质、尺寸比例
                                       - 表面纹理/装饰/特殊标记
                                       - 磨损/氧化/使用痕迹
                                       - 光影反射特征

                                    4、描绘字段禁止出现下面的内容：
                                       - 物品用途或功能描述
                                       - 背景故事或象征意义
                                       - 与人物相关的任何信息
                                       - 非视觉特征（如气味、声音）

                                    5、请输出剧本提及的道具及其状物描写，并且名称与描绘必须一一对应且数量相等

                                    6、输出示例：
                                        {
                                        "name":["皮医生的药箱","陈夫人的项链","王大爷的皮带",...]
                                        "description":[
                                            "方方正正，约莫一尺来长，半尺宽...",
                                            "链身极细，每一环都打磨得溜光水滑，在灯下泛着冷冽的银光...",
                                            "褐色的牛皮表面布满细密的纹路，带身约莫三指宽，边缘处已经被磨得发亮...",
                                            ...
                                        ]}

                                    请根据用户提供的剧本，以JSON格式输出所有道具的描绘
                    """ };

    private static final String AnalyzePrompt = """
             	你是一名剧本杀质量评估员，请遵循以下规则对用户的剧本杀进行评估：
                        1、按指定MarkDown格式输出以下字段：
            		标题
                            剧本亮点（数组）
                            游玩难点（数组）
                            改进建议（数组）
                            综合评分（包含逻辑性、故事性、体验感、总体得分四项整数评分，这部分内容无需输出分析部分）

                        输出需符合如下结构：
            		# 剧本分析报告

            		## ✨ 剧本亮点
            		    ...
            		---
            		## ⚠️ 游玩难点
            		    ...
            		---
            		## 💡 改进建议
            		    ...
            		---
            		## ⭐ 综合评分
            		|维度|评分（满分100）|
            		|:-------:|:-----------------:|
            		|逻辑性|对应分数|
            		|故事性|对应分数|
            		|体验感|对应分数|
            		|总体得分|对应分数|

                        2、亮点、难点、改进意见的表达需要足够详尽

                        3、评分部分满分为100，评分标准如下：
                            逻辑性：对剧本故事、人物行为的逻辑性评分，如果人物/事件发展不符合现实逻辑，应给予低分
                            故事性：评分规则如下：
            			（1）加分项：叙事风格包含心理/环境描写
            			（2）加分项：角色动机含多重因素（如"经济压力+历史恩怨+情感纠葛"）
            			（3）加分项：剧情具有多层反转
            			（4）加分项：线索描绘与剧情相融合
                                    （5）扣分项：对剧本的描述过于简单
                            体验感：推测每个人物剧本所带给玩家体验性的预测评分，如果某一角色参与度很低，应给予低分
            		总体得分：综合上述三项进行评分

            		参考案例：下面剧本的逻辑性得分：70，故事性得分：60，体验感得分：50，总体得分：65
            雨夜钟声
            背景
            1935年秋，苏州城郊「福临客栈」因暴雨歇业。掌柜李世昌深夜遇害于账房，胸口插着断裂的铜制怀表指针，屋内老式座钟停摆于凌晨2:15。六名住客被困客栈，暴雨冲断山路前，需厘清命案真相。

            人物剧本:
            - CHR 赵文远（男，账房先生）价值观：信奉“钱财身外物，性命最要紧”。背景：因挪用公款被李世昌威胁，携家眷投宿客栈。案发前与掌柜争吵，要求结算工钱。时间线：19:00与掌柜对账→21:30回房→22:00听见钟声→未离开房间。信息差：不知掌柜曾私下克扣其他伙计工钱。
            - CHR 苏红袖（女，客栈厨师）价值观：“情义比银钱重，恨比爱长久”。背景：暗恋李世昌之女李婉儿，因身份悬殊未表白。案发前夜为李婉儿送醒酒汤。时间线：20:00熬药→21:50倒药渣→22:10回灶台取烛台。信息差：知晓掌柜私藏鸦片于灶房梁上。
            - CHR 李婉儿（女，掌柜之女）价值观：“宁可撕破脸，不可吃亏”。背景：因父亲逼婚富商之子，赌气带丫鬟投靠客栈。案发时正与丫鬟缝补嫁衣。时间线：20:30骂跑说亲媒人→22:00绣花→22:15听见瓷器碎裂声。信息差：发现父亲账本记录自己嫁妆被挪用。
            - CHR 陈阿四（男，客栈杂役）价值观：“活着比什么都强”。背景：聋哑人，被掌柜收留。案发前擦拭大堂铜钟，目睹掌柜与黑衣人争执。时间线：19:30擦钟→21:00喂狗→22:00躺柴房。信息差：看见苏红袖深夜进入后院竹林。
            - CHR 王警长（男，巡警队长）价值观：“规矩比人重要”。背景：伪装成住客调查客栈走私案。案发后第一时间封锁现场。时间线：20:45登记住客→22:05听到钟声→22:10破门而入。信息差：携带记录客栈走私名单的密信。
            - CHR 刘麻子（男，落魄货郎）
            价值观：“墙头草两边倒”。背景：欠赌债躲进客栈。案发时偷喝柜台酒，见掌柜尸体后藏起铜钥匙。时间线：21:00撬柜台→21:50醉酒→22:05呕吐在院中。信息差：捡到半张烧焦的婚约文书。

            线索
            - C> 【铜钟指针】：断裂处有新鲜血迹与指纹，内侧刻“福临”字样（指向凶器来源）。- C> 【账本残页】：夹在《论语》中，记录“苏红袖借支五块”“李婉儿嫁妆短少十块”（暗示经济矛盾）。- C> 【灶房烟灰】：含罂粟壳碎片，与李世昌指甲缝残留物一致（指向毒品交易）。- C> 【绣花针盒】：李婉儿房间掉落，针尖沾蓝墨水（与婚约文书字迹同色）。- C> 【密信残角】：王警长口袋露出“李世昌勾结盐商”字样（暗示其死亡牵连更大阴谋）。- C> 【竹叶露水】：后院竹林采集，检测出麻沸散成分（与苏红袖药锅药材吻合）。

            真相
            凶手：苏红袖。作案过程：苏红袖因爱生恨，利用灶房鸦片与麻沸散制作毒药，假借送汤下毒。
            22:00将昏迷掌柜拖至账房，用铜钟指针刺死，布置成自杀假象。利用座钟停摆制造不在场证明，实际通过灶台暗门往返现场。关键诡计：竹叶上的露水实为下毒时蹭落的汗液，与陈阿四擦拭的铜钟形成时间差证据。

            组织者手册
            流程指引：强调时间线对比（赵文远争吵时间vs苏红袖送汤时间）。引导分析线索关联性（账本矛盾指向经济动机，竹叶露水指向下毒手法）。揭露王警长身份时，需同步公开密信内容以解释其隐瞒行为。最终推理需串联“麻沸散-竹林-铜钟”三重证据链。新手提示：重点排查角色与死者的直接利益冲突（如苏红袖情感、赵文远债务）。注意“聋哑人陈阿四”证词需通过手势比划还原，增强沉浸感。李婉儿嫁衣绣线颜色与婚约文书墨迹的对应关系可作为辅助推理点。
                                    """;
}