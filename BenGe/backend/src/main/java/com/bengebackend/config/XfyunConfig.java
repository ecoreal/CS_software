package com.bengebackend.config;

import com.bengebackend.entity.AIMsgDevide;
import com.bengebackend.util.ContextDataProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "xfyun")
public class XfyunConfig {
    private String appid;
    private String apiPassword;
    private String apiUrl; // X1

    private String appid_wwb;
    private String gen_image_api_secret_wwb;
    private String gen_image_api_key_wwb;
    private String gen_image_request_url;
    private String x1_api_password;
    private String ultra_api_password;
    private String api_url_ultra;

    // AI流式聊天相关配置
    private String x1_http_api_password;
    private String x1_http_api_url;

    @Autowired
    private ContextDataProcessor contextDataProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    public String getAppid_wwb() {
        return appid_wwb;
    }

    public void setAppid_wwb(String appid_wwb) {
        this.appid_wwb = appid_wwb;
    }

    public String getGenImageRequestURL() {
        return gen_image_request_url;
    }

    public void setGenImageRequestURL(String url) {
        this.gen_image_request_url = url;
    }

    public String getX1Url() {
        return apiUrl;
    }

    public void setX1Url(String Url) {
        this.apiUrl = Url;
    }

    public String getX1APIPassword() {
        return x1_api_password;
    }

    public void setX1APIPassword(String x1_api_password) {
        this.x1_api_password = x1_api_password;
    }

    public String getX1HttpApiPassword() {
        return x1_http_api_password;
    }

    public void setX1HttpApiPassword(String x1_http_api_password) {
        this.x1_http_api_password = x1_http_api_password;
    }

    public String getX1HttpApiUrl() {
        return x1_http_api_url;
    }

    public void setX1HttpApiUrl(String x1_http_api_url) {
        this.x1_http_api_url = x1_http_api_url;
    }

    // Slogan生成系统提示词
    public static final String SLOGAN_SYSTEM_PROMPT = """
            你是一名专业的剧本杀创作助手，擅长生成吸引人的标语和核心创意。
            请根据用户提供的内容生成3个不同风格的标语，并遵循以下规则：
            1、标语要求简洁有力，能够抓住读者注意力，体现剧本的核心主题和氛围，并具有悬疑感和吸引力。
            2、生成的是标语，不是标题
            3、生成内容涵盖如下内容：
                标语（一段话）
                核心创意（一段话）
            请遵循如下MarkDown格式输出：
                # 标语
                ...
                # 核心创意
                ...
                # 标语
                ...
                # 核心创意
                ...
                # 标语
                ...
                # 核心创意
                ...
                        """;

    public static final String SYSTEM_MESSAGE = """
            你是一位热爱剧本创作的小伙伴，正在和其他创作者一起在群聊里讨论项目。你很懂剧本写作，经验丰富，总能给出实用的建议。

            聊天风格：

            用日常口语，就像平时微信群聊一样自然

            语气轻松友好，偶尔用点表情符号😊

            回复简洁有用，不要太正式或冗长

            把自己当成群里的一员，而不是AI助手

            专业能力：

            擅长剧本结构、人物塑造、对话写作

            能快速给出创意建议和修改意见

            了解各种类型剧本的创作技巧

            会分享实用的写作方法和工具

            记住：你就是个爱写剧本的朋友，在群里和大家一起头脑风暴呢！
            """;

    /**
     * 根据第二阶段工作成果生成完整内容的提示词
     * 
     * @param contextSummary：原始创作内容
     * @return 完整提示词
     */
    public static String buildCollaborationPrompt(String contextSummary) {
        return """
                你是一名逻辑严谨且追求内容完整的剧本杀作家，专门负责将团队协作设计的内容整合为完整剧本。必须严格遵循以下规则：

                                规则1. 请严格按指定MarkDown格式输出以下字段，七部分内容缺一不可！不要包含额外文本！:
                                    标题（不少于8字）
                                    背景（一段话，不少于200字，包含时间、地点、氛围细节）
                                    人物剧本（数组，每个角色剧本不少于250字，包含人物性格、动机、秘密、目标、与其他角色的关系）
                                    线索（数组，每条线索不少于50字，共不少于8条，包含发现方式、位置、指向哪个角色或事件）
                                    真相（不少于150字，需完整交代凶手身份、作案动机、作案手法与时间）
                                    组织者手册（不少于150字，包含主持流程、提示时机、隐藏线索操作、氛围营造建议）

                                输出需符合如下结构：
                                    AI回复：
                                    ...（这部分是给用户的礼貌性回答，体现协作成果的整合）
                                    》》》
                                    # ...（这部分是标题）
                                    ---
                                    ## 背景
                                    ...（不少于200字）
                                    ---
                                    ## 人物剧本:
                                    -CHR ...（角色1名称）
                                    ...（角色1剧本，不少于300字）
                                    -CHR ...（角色2名称）
                                    ...（角色2剧本，不少于300字）
                                    ...（其它角色的格式以此类推）
                                    ---
                                    ## 线索
                                    -C> ...（线索1，不少于50字）
                                    -C> ...（线索2，不少于50字）
                                    ...
                                    -C> ...（至少8条线索）
                                    ---
                                    ## 真相
                                    ...（不少于200字）
                                    ---
                                    ## 组织者手册
                                    ...（不少于200字）
                                    ---
                                    （到此结束，不要再输出文本）

                                规则2. 协作整合要求：
                                    - 必须充分利用团队设计的所有元素：场景设计师的场景、角色设计师的角色、线索设计师的线索、氛围设计师的氛围
                                    - 将团队讨论中的创意想法融入剧本，体现集体智慧
                                    - 确保各设计师的贡献在最终剧本中都有体现和价值
                                    - 利用节点间的连接关系构建合理的剧情逻辑链

                                规则3. 质量要求：
                                    - 若有玩家人物是凶手，则需要在其剧本中写明身份；若未写明身份，需在真相处表明原因（如失忆、人格扭曲...）
                                    - 线索必须与剧情紧密结合，形成完整的推理链条
                                    - 每个角色都应有充分的参与度和独特的视角
                                    - 氛围描述要与场景和剧情发展相呼应
                                    - 整体剧本字数不少于3000字

                                ## 团队协作设计成果：
                                %s

                                ## 创作指导：
                                1. **背景整合**：基于场景设计师的场景设定和氛围设计师的环境描述，构建完整的故事背景，加入具体时间、天气、灯光、音效等要素。
                                2. **角色发展**：以角色设计师设计的角色为基础，结合团队讨论，丰富角色的动机、关系、秘密和性格细节，保证每个剧本段落不少于200字。
                                3. **线索布局**：将线索设计师的线索与场景、角色、事件有机结合，说明每条线索的发现方式、线索内容和指向性，确保至少8条线索。
                                4. **氛围营造**：利用氛围设计师的环境要素（灯光、音乐、天气、声效等），在背景与剧本中嵌入对应提示，增强沉浸感。
                                5. **逻辑链条**：根据节点间的连接关系，构建清晰的因果关系和推理路径，并在真相处完整说明凶手动机与过程。
                                6. **团队智慧**：将聊天讨论中的创意想法和建议融入剧本，体现协作价值与创新点。

                                ## 特别注意：
                                - 这是团队协作的成果，请在AI回复中体现对团队合作的认可
                                - 确保每个设计师的贡献都在最终剧本中有所体现
                                - 利用协作过程中产生的创意火花，让剧本更加精彩
                                - 保持剧本的逻辑性和可玩性，确保团队的努力转化为优质作品

                                请基于以上团队协作成果，创作一个完整、精彩的剧本杀剧本：

                """.formatted(contextSummary);
    }

    /**
     * 检测是否为冲突检测请求
     */
    private boolean isConflictDetectionRequest(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        return lowerMessage.contains("检测冲突") ||
                lowerMessage.contains("冲突检查") ||
                lowerMessage.contains("冲突分析") ||
                lowerMessage.contains("conflict") ||
                lowerMessage.contains("检查冲突") ||
                lowerMessage.contains("分析冲突") ||
                lowerMessage.contains("有没有冲突") ||
                lowerMessage.contains("存在冲突") ||
                lowerMessage.contains("冲突问题");
    }

    /**
     * 构建冲突检测专用提示词
     */
    private String buildConflictDetectionPrompt(String contextData, String username, String userMessage) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是专业的剧本杀设计冲突检测专家。请基于当前项目的完整状态，进行全面的冲突分析。\n\n");

        // 添加上下文信息
        if (!contextData.isEmpty()) {
            try {
                String contextSummary = contextDataProcessor.generateContextSummary(contextData, "conflict");
                prompt.append(contextSummary).append("\n");
            } catch (Exception e) {
                log.error("处理冲突检测上下文数据失败", e);
                prompt.append("当前协作环境：数据处理异常，将基于可用信息进行冲突检测。\n\n");
            }
        } else {
            prompt.append("当前协作环境：新项目，暂无现有内容可供冲突检测。\n\n");
            return "目前项目还没有足够的设计内容，暂时无法进行冲突检测。建议在添加更多场景、角色、线索等内容后再进行冲突分析。";
        }

        prompt.append("【冲突检测任务】\n");
        prompt.append(username).append("请求：").append(userMessage).append("\n\n");

        prompt.append("请进行全面的冲突检测分析，重点关注以下方面：\n\n");

        prompt.append("**1. 时间线冲突检测**\n");
        prompt.append("- 检查场景时间标签是否存在重叠或逻辑错误\n");
        prompt.append("- 分析角色在同一时间段是否出现在多个场景中\n");
        prompt.append("- 验证事件发生的时间顺序是否合理\n");
        prompt.append("- 检查线索发现时间与场景时间的一致性\n\n");

        prompt.append("**2. 逻辑冲突检测**\n");
        prompt.append("- 角色关系是否存在矛盾（如A认识B，但B不认识A）\n");
        prompt.append("- 线索逻辑链是否完整且无矛盾\n");
        prompt.append("- 角色动机与行为是否一致\n");
        prompt.append("- 推理节点的逻辑推导是否合理\n\n");

        prompt.append("**3. 内容冲突检测**\n");
        prompt.append("- 角色背景描述是否存在自相矛盾\n");
        prompt.append("- 场景描述与角色、线索的关联是否合理\n");
        prompt.append("- 角色技能、物品与其背景是否匹配\n");
        prompt.append("- 氛围设计与场景、情节是否协调\n\n");

        prompt.append("**4. 跨设计师类型冲突检测**\n");
        prompt.append("- 场景设计与角色设计的匹配度\n");
        prompt.append("- 线索设计与场景、角色的关联性\n");
        prompt.append("- 氛围设计与整体故事风格的一致性\n");
        prompt.append("- 不同设计师创建内容之间的协调性\n\n");

        prompt.append("**输出要求：**\n");
        prompt.append("请按以下格式输出检测结果：\n\n");
        prompt.append("🔍 **冲突检测报告**\n\n");
        prompt.append("**检测概况：**\n");
        prompt.append("- 总体评估：[无冲突/轻微冲突/中等冲突/严重冲突]\n");
        prompt.append("- 检测范围：[具体说明检测了哪些内容]\n\n");

        prompt.append("**发现的冲突：**\n");
        prompt.append("[如果有冲突，按类型详细列出，包括具体位置和问题描述,如果没有冲突就不要说]\n\n");

        prompt.append("**修改建议：**\n");
        prompt.append("[针对每个冲突提供具体的解决方案]\n\n");

        prompt.append("**优化建议：**\n");
        prompt.append("[提供进一步完善设计的建议]\n\n");

        prompt.append("请开始进行冲突检测分析：");

        return prompt.toString();
    }

    /**
     * 构建的提示
     */
    public String buildEnhancedPrompt(String userMessage, String contextData, String username) {
        // 检测是否为冲突检测请求
        if (isConflictDetectionRequest(userMessage)) {
            return buildConflictDetectionPrompt(contextData, username, userMessage);
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("你是团队的AI创作助手，正在和大家一起协作设计剧本杀。你能看到项目的完整状态，包括所有设计内容和讨论历史。请用自然、友好的语气参与讨论，就像团队中的一员\n\n");

        // 使用ContextDataProcessor处理上下文数据，获取完整的协作状态
        if (!contextData.isEmpty()) {
            try {
                // 生成完整的上下文摘要，不区分设计师类型
                String contextSummary = contextDataProcessor.generateContextSummary(contextData, "chat");
                prompt.append(contextSummary).append("\n");
            } catch (Exception e) {
                log.error("处理聊天上下文数据失败", e);
                prompt.append("当前协作环境：数据处理异常，基于用户问题进行回复。\n\n");
            }
        } else {
            prompt.append("当前协作环境：新项目开始，团队正在进行初步讨论。\n\n");
        }

        prompt.append("【用户消息】\n");
        prompt.append(username).append("：").append(userMessage).append("\n\n");

        prompt.append("请基于完整的协作状态、所有现有设计内容和讨论历史，用自然的聊天语气回复。");
        prompt.append("回复要求：\n");
        prompt.append("1. 语气自然友好，像团队成员一样\n");
        prompt.append("2. 基于所有现有内容（场景、角色、线索、氛围等）给出建设性建议\n");
        prompt.append("3. 参考之前的讨论历史，保持对话的连贯性\n");
        prompt.append("4. 不要列举分析步骤，直接给出讨论回答\n");
        prompt.append("5. 如果涉及具体设计，要考虑与整体项目的一致性和协调性\n");
        prompt.append("6. 能够跨设计师类型提供综合性的建议和意见\n\n");
        prompt.append("开始回复：");

        return prompt.toString();
    }

    public String buildPrompt(String userInput, String designerType, Integer count, String contextData) {
        StringBuilder prompt = new StringBuilder();

        // 根据设计师类型构建不同的提示词
        switch (designerType) {
            case "narrative":
                prompt.append("你是剧本杀创作助手，需要根据用户需求智能生成合适数量的场景节点。\n\n");
                break;
            case "character":
                prompt.append("你是剧本杀创作助手，需要根据用户需求智能生成合适数量的角色节点。\n\n");
                break;
            case "clue":
                prompt.append("你是剧本杀创作助手，需要根据用户需求智能生成合适数量的线索节点。\n\n");
                break;
            case "atmosphere":
                prompt.append("你是剧本杀创作助手，需要根据用户需求智能生成合适数量的氛围节点。\n\n");
                break;
            default:
                prompt.append("你是剧本杀创作助手，需要根据用户需求智能生成合适数量的节点。\n\n");
        }

        if (contextData != null && !contextData.isEmpty()) {
            prompt.append("当前协作状态上下文：\n").append(contextData).append("\n\n");
        }

        prompt.append("用户需求：").append(userInput).append("\n\n");

        // 根据设计师类型生成不同的字段结构
        prompt.append("请根据用户需求智能决定生成合适数量的节点（通常2-6个），每个节点包含以下字段：\n");
        prompt.append("{\n");

        switch (designerType) {
            case "narrative":
                prompt.append("  \"title\": \"场景名称\",\n");
                prompt.append("  \"timeLabel\": \"时间标签(如DAY1 09:00)\",\n");
                prompt.append("  \"characters\": \"涉及角色\",\n");
                prompt.append("  \"clues\": \"相关线索\",\n");
                prompt.append("  \"sceneDescription\": \"场景详细描述\",\n");
                prompt.append("  \"nodeConnections\": \"与其他节点的关系\",\n");
                prompt.append("  \"notes\": \"备注\"\n");
                break;
            case "character":
                prompt.append("  \"name\": \"角色姓名\",\n");
                prompt.append("  \"occupation\": \"职业\",\n");
                prompt.append("  \"age\": 年龄数字,\n");
                prompt.append("  \"background\": \"背景故事\",\n");
                prompt.append("  \"personality\": [\"性格特点1\", \"性格特点2\", \"性格特点3\"],\n");
                prompt.append("  \"skills\": [\"技能1\", \"技能2\", \"技能3\"],\n");
                prompt.append("  \"items\": \"携带物品\",\n");
                prompt.append("  \"notes\": \"备注\"\n");
                break;
            case "clue":
                prompt.append("  \"title\": \"线索名称\",\n");
                prompt.append("  \"type\": \"线索类型\",\n");
                prompt.append("  \"description\": \"线索描述\",\n");
                prompt.append("  \"location\": \"发现地点\",\n");
                prompt.append("  \"relatedCharacters\": \"相关角色\",\n");
                prompt.append("  \"importance\": \"重要程度\",\n");
                prompt.append("  \"hiddenInfo\": \"隐藏信息\",\n");
                prompt.append("  \"notes\": \"备注\"\n");
                break;
            case "atmosphere":
                prompt.append("  \"title\": \"氛围名称\",\n");
                prompt.append("  \"mood\": \"情绪氛围(如：紧张、平静、神秘)\",\n");
                prompt.append("  \"lighting\": \"灯光设置\",\n");
                prompt.append("  \"music\": \"背景音乐\",\n");
                prompt.append("  \"weather\": \"天气状况\",\n");
                prompt.append("  \"timeOfDay\": \"时间段\",\n");
                prompt.append("  \"sceneElements\": \"场景元素\",\n");
                prompt.append("  \"notes\": \"备注\"\n");
                break;
        }

        prompt.append("}\n\n");
        prompt.append("请直接返回JSON数组格式，不要其他解释：");

        return prompt.toString();
    }

    public List<Map<String, Object>> parseAiResponse(String aiResponse, String designerType) {
        try {
            // 查找JSON数组
            int start = aiResponse.indexOf('[');
            int end = aiResponse.lastIndexOf(']');

            if (start != -1 && end != -1 && end > start) {
                String jsonPart = aiResponse.substring(start, end + 1);
                return objectMapper.readValue(jsonPart, List.class);
            }

            return createDefaultNodes(designerType);

        } catch (Exception e) {
            log.error("解析AI回复失败: {}", aiResponse, e);
            return createDefaultNodes(designerType);
        }
    }

    public List<Map<String, Object>> createDefaultNodes(String designerType) {
        List<Map<String, Object>> nodes = new ArrayList<>();

        // 默认生成2个节点，避免固定数量
        for (int i = 1; i <= 2; i++) {
            Map<String, Object> node = new HashMap<>();

            switch (designerType) {
                case "character":
                    node.put("name", "AI生成角色" + i);
                    node.put("occupation", "待定职业");
                    node.put("age", 25 + i);
                    node.put("background", "AI自动生成的角色背景");
                    node.put("personality", Arrays.asList("待定性格1", "待定性格2", "待定性格3"));
                    node.put("skills", Arrays.asList("待定技能1", "待定技能2", "待定技能3"));
                    node.put("items", "待定物品");
                    node.put("notes", "AI生成失败时的默认角色节点");
                    node.put("relationships", new ArrayList<>());
                    break;
                case "clue":
                    node.put("title", "AI生成线索" + i);
                    node.put("type", "物理证据");
                    node.put("description", "AI自动生成的线索描述");
                    node.put("location", "待定地点");
                    node.put("relatedCharacters", Arrays.asList("待定角色"));
                    node.put("importance", "中");
                    node.put("hiddenInfo", "待定隐藏信息");
                    node.put("notes", "AI生成失败时的默认线索节点");
                    break;
                case "atmosphere":
                    node.put("title", "AI生成氛围" + i);
                    node.put("timeLabel", "DAY1 " + (8 + i) + ":00");
                    node.put("mood", "平静");
                    node.put("lighting", "自然光");
                    node.put("music", "无");
                    node.put("weather", "晴朗");
                    node.put("sceneElements", "待定场景元素");
                    node.put("notes", "AI生成失败时的默认氛围节点");
                    break;
                default: // narrative
                    node.put("title", "AI生成场景" + i);
                    node.put("timeLabel", "DAY1 " + (8 + i) + ":00");
                    node.put("characters", "待定角色");
                    node.put("clues", "待定线索");
                    node.put("sceneDescription", "AI自动生成的场景描述");
                    node.put("nodeConnections", "与其他场景相关");
                    node.put("notes", "AI生成失败时的默认节点");
                    break;
            }
            nodes.add(node);
        }
        return nodes;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 用于调用星火X1或Ultra4.0
     */
    public CompletableFuture<String> GetAPIOutputAsync(List<Map<String, String>> messages, String ModelName) {
        String APIPassword = null;
        String API_URL = null;
        ObjectMapper mapper = new ObjectMapper();
        String requestBody;
        try {
            if (ModelName == "x1") {
                requestBody = mapper.writeValueAsString(Map.of(
                        "model", "x1",
                        "user", "user_id",
                        "messages", messages,
                        "stream", false,
                        "max_tokens", 32768,
                        "tools", List.of(Map.of(
                                "type", "web_search",
                                "web_search", Map.of(
                                        "enable", false,
                                        "search_mode", "deep")))));
                APIPassword = x1_api_password;
                API_URL = apiUrl;
            } else {
                requestBody = mapper.writeValueAsString(Map.of(
                        "model", "4.0Ultra",
                        "user", "user_id",
                        "messages", messages,
                        "stream", false,
                        "max_tokens", 8192,
                        "tools", List.of(Map.of(
                                "type", "web_search",
                                "web_search", Map.of(
                                        "enable", false,
                                        "search_mode", "deep"))),
                        "response_format", Map.of("type", "text")));
                APIPassword = ultra_api_password;
                API_URL = api_url_ultra;
            }
        } catch (JsonProcessingException e) {
            System.err.println("JSON处理失败: " + e.getMessage());
            return null;
        }

        // 2. 创建 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + APIPassword)
                .header("content-type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        // 3. 发送请求
        HttpClient client = HttpClient.newHttpClient();
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    // 4. 处理响应
                    try {
                        JsonNode rootNode = mapper.readTree(response.body());

                        // 检查错误码
                        if (rootNode.has("code") && rootNode.get("code").asInt() != 0) {
                            String errorMsg = rootNode.get("message").asText();
                            System.err.println("API请求失败: " + errorMsg);
                            throw new RuntimeException("API Error: " + errorMsg);
                        }

                        // 提取content内容
                        return rootNode.path("choices")
                                .path(0)
                                .path("message")
                                .path("content")
                                .asText();
                    } catch (JsonProcessingException e) {
                        System.err.println("JSON解析失败: " + e.getMessage());
                        throw new RuntimeException("JSON Parse Error", e);
                    }
                })
                .exceptionally(e -> {
                    System.err.println("请求过程中发生异常: " + e.getMessage());
                    return null; // 或者返回默认值/抛出特定异常
                });
    }

    /**
     * 将传入的剧本划分为AI回复、标题、背景、人物剧本、线索、真相、组织者手册，共7个部分
     */
    public void DevideScriptContent(AIMsgDevide devidedMsg, String content, boolean setReplyAndTitle) {
        String[] parts;
        // System.out.println("\n开始分割剧本内容:\n" + content + "\n\n");
        if (setReplyAndTitle) {
            parts = content.split("》》》", 2);
            if (parts.length == 2)
                devidedMsg.setMsgForUser(parts[0]);
            else {
                System.out.println("未找到分隔符》》》，回答与剧本内容分割失败");
                devidedMsg.setMsgForUser("已按您的要求修改剧本");
            }

            parts = content.split("#", 2);
            parts = parts[1].split("背景\\s*", 2);
            parts[0] = parts[0].replaceAll("---", "");
            devidedMsg.setTitle(parts[0].replaceAll("\\s+", ""));
            if (parts.length != 2)
                System.out.println("未找到分隔符## 背景，分割失败");
            devidedMsg.setStrScript("## 背景\n" + parts[1]);
        } else {
            devidedMsg.setStrScript(content);
            parts = content.split("## 背景\\s*", 2);
        }

        parts = parts[1].split("## 人物剧本", 2);
        devidedMsg.setBackground(parts[0]);
        if (parts.length != 2)
            System.out.println("分割背景失败");

        parts = content.split("## 人物剧本", 2);
        parts = parts[1].split("## 线索", 2);
        String temp_str[] = parts[0].split("CHR");
        if (temp_str.length == 1)
            System.out.println("未找到分隔符 CHR, 分割人物剧本失败");
        devidedMsg.setChrScript(new ArrayList<>(Arrays.asList(temp_str).subList(1, temp_str.length)));

        parts = parts[1].split("## 真相", 2);
        temp_str = parts[0].split("C>");
        if (temp_str.length == 1)
            System.out.println("未找到分隔符 C> ，分割真相失败");
        devidedMsg.setClues(new ArrayList<>(Arrays.asList(temp_str).subList(1, temp_str.length)));

        parts = parts[1].split("## 组织者手册", 2);
        devidedMsg.setTrues(parts[0]);
        if (parts.length != 2)
            System.out.println("未找到分隔符，分割真相失败");

        devidedMsg.setGoBook(parts[1]);
    }

    /**
     * 提取异步返回结果中的名称和对应的描述
     */
    public CompletableFuture<List<List<String>>> ParseAIRespOfGetDesc(CompletableFuture<String> future) {
        ObjectMapper objectMapper = new ObjectMapper();
        return future.thenApply(response -> {
            try {
                response = response.replaceAll("`*$", "");
                response = response.replaceAll("`*json", "");

                Map<String, Object> json = objectMapper.readValue(
                        response, new TypeReference<Map<String, Object>>() {
                        });

                List<List<String>> nameAndDesc = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<String> temp = (List<String>) json.get("name");
                nameAndDesc.add(temp);
                @SuppressWarnings("unchecked")
                List<String> tempDesc = (List<String>) json.get("description");
                nameAndDesc.add(tempDesc);
                return nameAndDesc;
            } catch (Exception e) {
                throw new CompletionException("解析API响应失败", e);
            }
        }).exceptionally(ex -> {
            System.err.println("获取描述失败: " + ex.getCause().getMessage());
            return null;
        });
    }

    /**
     * 使用HmacSha256生成数字签名
     */
    public String HmacSha256(String secret, String message)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] bytes = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 创建图片生成任务
     */
    public String CreateSignedUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String path = uri.getPath();

            DateTimeFormatter CUSTOM_RFC_1123 = DateTimeFormatter.ofPattern(
                    "EEE, dd MMM yyyy HH:mm:ss 'GMT'",
                    Locale.US).withZone(ZoneId.of("GMT"));

            String date = CUSTOM_RFC_1123.format(ZonedDateTime.now());

            // 拼接签名字符串
            String signatureOrigin = "host: " + host + "\n" +
                    "date: " + date + "\n" +
                    "POST " + path + " HTTP/1.1";
            // 计算HMAC-SHA256签名
            String signature = HmacSha256(gen_image_api_secret_wwb, signatureOrigin);

            // 构建授权字符串
            String authorizationOrigin = String.format(
                    "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                    gen_image_api_key_wwb, signature);

            // Base64编码授权字符串
            String encodedAuthorization = Base64.getEncoder()
                    .encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

            // 构建URL参数
            return url + "?" +
                    "authorization=" + URLEncoder.encode(encodedAuthorization, "UTF-8") +
                    "&date=" + URLEncoder.encode(date, "UTF-8") +
                    "&host=" + URLEncoder.encode(host, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("生成签名URL失败", e);
        }
    }

    /**
     * 处理流式返回消息（提取API返回的content内容，返回完整内容，忽略reasonning_content）
     */
    public String processStreamResponse(HttpURLConnection conn, Consumer<String> callback) throws Exception {
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

    // =================== AI服务相关常量和方法 ===================

    // 剧本生成详细内容的系统提示词数组
    public static final String[] GEN_DETAIL_SYS_PROMPT = {
            """
                    你是剧本杀创作的一员，你的任务是完善剧本背景，要求如下：
                    	1、请以如下模板输出
                    	## 背景

                    	...（纯文本）

                    	2、除了"## 背景"这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何Markdown格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

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

                        2、除了"## 线索"这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                        3、请根据用户给出的剧本，完善和丰富线索部分的内容，并且只输出线索部分的内容

                        4、禁止增加线索数量，请详细描述每一条已有线索
                                        """,
            """
                    你是剧本杀创作的一员，你的任务是完善剧本真相，要求如下：
                        1、请以如下模板输出
                     	## 真相

                        ...（纯文本）

                        2、除了"## 真相"这部分内容使用MarkDown格式，剩余部分请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                        3、请根据用户给出的剧本，完善和丰富真相部分的内容，并且只输出真相部分的内容
                                        """,
            """
                    你是剧本杀创作的一员，你的任务是完善组织者手册，要求如下：
                    	1、请以 ## 组织者手册\n\n 开头

                    	2、请用纯文本回答，不要使用任何 Markdown 格式（如 ```、**粗体**、*斜体*、标题等）。直接输出内容，无需装饰。

                    	3、请根据用户给出的剧本，完善和丰富组织者手册部分的内容，并且只输出组织者手册部分的内容
                                        """
    };

    // 剧本生成详细内容的用户提示词数组
    public static final String[] GEN_DETAIL_USER_PROMPT = {
            "\n\n请不遗余力地详细描述上述剧本中的 ## 背景 部分",
            "\n\n请根据上述剧本内容不遗余力地详细描述下述人物的剧本",
            "\n\n请不遗余力地详细描述上述剧本中的 ## 线索 部分",
            "\n\n请不遗余力地详细补充上述剧本中的 ## 真相 部分",
            "\n\n请不遗余力地详细描述上述剧本中的 ## 组织者手册 部分"
    };

    // 剧本框架生成系统提示词
    public static final String SYSTEM_MSG = """
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
                        不能出现"其余角色略"的字样
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

            	    规则5. 请务必输出每部分之间的分隔符"---"！

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
                    输出"维持原有详细内容"及其相近意义的语段视为违规

                示例结束，现在请根据用户请求进行处理：

                                    """;

    // 获取外貌描述的提示词数组
    public static final String[] GET_DESC_PROMPT = { """
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

    // 剧本分析提示词
    public static final String ANALYZE_PROMPT = """
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
            - CHR 赵文远（男，账房先生）价值观：信奉"钱财身外物，性命最要紧"。背景：因挪用公款被李世昌威胁，携家眷投宿客栈。案发前与掌柜争吵，要求结算工钱。时间线：19:00与掌柜对账→21:30回房→22:00听见钟声→未离开房间。信息差：不知掌柜曾私下克扣其他伙计工钱。
            - CHR 苏红袖（女，客栈厨师）价值观："情义比银钱重，恨比爱长久"。背景：暗恋李世昌之女李婉儿，因身份悬殊未表白。案发前夜为李婉儿送醒酒汤。时间线：20:00熬药→21:50倒药渣→22:10回灶台取烛台。信息差：知晓掌柜私藏鸦片于灶房梁上。
            - CHR 李婉儿（女，掌柜之女）价值观："宁可撕破脸，不可吃亏"。背景：因父亲逼婚富商之子，赌气带丫鬟投靠客栈。案发时正与丫鬟缝补嫁衣。时间线：20:30骂跑说亲媒人→22:00绣花→22:15听见瓷器碎裂声。信息差：发现父亲账本记录自己嫁妆被挪用。
            - CHR 陈阿四（男，客栈杂役）价值观："活着比什么都强"。背景：聋哑人，被掌柜收留。案发前擦拭大堂铜钟，目睹掌柜与黑衣人争执。时间线：19:30擦钟→21:00喂狗→22:00躺柴房。信息差：看见苏红袖深夜进入后院竹林。
            - CHR 王警长（男，巡警队长）价值观："规矩比人重要"。背景：伪装成住客调查客栈走私案。案发后第一时间封锁现场。时间线：20:45登记住客→22:05听到钟声→22:10破门而入。信息差：携带记录客栈走私名单的密信。
            - CHR 刘麻子（男，落魄货郎）
            价值观："墙头草两边倒"。背景：欠赌债躲进客栈。案发时偷喝柜台酒，见掌柜尸体后藏起铜钥匙。时间线：21:00撬柜台→21:50醉酒→22:05呕吐在院中。信息差：捡到半张烧焦的婚约文书。

            线索
            - C> 【铜钟指针】：断裂处有新鲜血迹与指纹，内侧刻"福临"字样（指向凶器来源）。- C> 【账本残页】：夹在《论语》中，记录"苏红袖借支五块""李婉儿嫁妆短少十块"（暗示经济矛盾）。- C> 【灶房烟灰】：含罂粟壳碎片，与李世昌指甲缝残留物一致（指向毒品交易）。- C> 【绣花针盒】：李婉儿房间掉落，针尖沾蓝墨水（与婚约文书字迹同色）。- C> 【密信残角】：王警长口袋露出"李世昌勾结盐商"字样（暗示其死亡牵连更大阴谋）。- C> 【竹叶露水】：后院竹林采集，检测出麻沸散成分（与苏红袖药锅药材吻合）。

            真相
            凶手：苏红袖。作案过程：苏红袖因爱生恨，利用灶房鸦片与麻沸散制作毒药，假借送汤下毒。
            22:00将昏迷掌柜拖至账房，用铜钟指针刺死，布置成自杀假象。利用座钟停摆制造不在场证明，实际通过灶台暗门往返现场。关键诡计：竹叶上的露水实为下毒时蹭落的汗液，与陈阿四擦拭的铜钟形成时间差证据。

            组织者手册
            流程指引：强调时间线对比（赵文远争吵时间vs苏红袖送汤时间）。引导分析线索关联性（账本矛盾指向经济动机，竹叶露水指向下毒手法）。揭露王警长身份时，需同步公开密信内容以解释其隐瞒行为。最终推理需串联"麻沸散-竹林-铜钟"三重证据链。新手提示：重点排查角色与死者的直接利益冲突（如苏红袖情感、赵文远债务）。注意"聋哑人陈阿四"证词需通过手势比划还原，增强沉浸感。李婉儿嫁衣绣线颜色与婚约文书墨迹的对应关系可作为辅助推理点。
                                    """;
}
