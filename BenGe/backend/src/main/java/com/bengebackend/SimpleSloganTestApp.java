package com.bengebackend;

import com.bengebackend.config.XfyunConfig;
import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.service.serviceImpl.AIServicelmpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 简单的Slogan流式生成测试应用
 * 可以直接运行，不需要Spring Boot环境
 */
public class SimpleSloganTestApp {

    private static final String SEPARATOR = "=".repeat(50);

    public static void main(String[] args) {
        System.out.println(SEPARATOR);
        System.out.println("🚀 GenerateSloganStreamAsync 测试工具");
        System.out.println(SEPARATOR);
        System.out.println("💡 这个工具用于测试AI流式生成Slogan功能");
        System.out.println("📝 输入剧本描述，获取AI生成的标语和核心创意");
        System.out.println();

        // 初始化服务
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        XfyunConfig xfyunConfig = new XfyunConfig();
        AIServicelmpl aiService = new AIServicelmpl(restTemplate, objectMapper, xfyunConfig);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("请选择操作:");
            System.out.println("1. 生成Slogan");
            System.out.println("2. 查看示例");
            System.out.println("3. 退出");
            System.out.print("请输入选择 (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runSloganGeneration(scanner, aiService);
                    break;
                case "2":
                    showExamples();
                    break;
                case "3":
                    System.out.println("👋 再见！");
                    return;
                default:
                    System.out.println("❌ 无效选择，请重试！");
            }
        }
    }

    /**
     * 运行Slogan生成
     */
    private static void runSloganGeneration(Scanner scanner, AIServicelmpl aiService) {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("🎯 Slogan生成模式");
        System.out.println("=".repeat(30));

        System.out.print("请输入剧本描述: ");
        String prompt = scanner.nextLine().trim();

        if (prompt.isEmpty()) {
            System.out.println("❌ 剧本描述不能为空！");
            return;
        }

        System.out.println("\n📝 您输入的剧本描述:");
        System.out.println("「" + prompt + "」");
        System.out.println();

        // 创建请求
        SloganRequestEntity request = new SloganRequestEntity(prompt, 1);

        // 创建输出处理器
        SloganOutputHandler outputHandler = new SloganOutputHandler();

        System.out.println("🔄 正在生成Slogan，请稍候...");
        System.out.println("-".repeat(50));

        try {
            // 调用AI服务
            CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(
                    request,
                    outputHandler::handleOutput);

            // 等待完成
            future.get();

            System.out.println("\n" + "-".repeat(50));
            System.out.println("✅ 生成完成！");

            // 显示统计信息
            outputHandler.showStatistics();

        } catch (Exception e) {
            System.err.println("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n按回车键继续...");
        scanner.nextLine();
    }

    /**
     * 显示示例
     */
    private static void showExamples() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("📚 剧本描述示例");
        System.out.println("=".repeat(30));

        String[] examples = {
                "一个发生在现代都市的悬疑推理剧本，涉及连环杀手和心理博弈",
                "古代宫廷斗争，充满权力与爱情的纠葛，暗藏阴谋诡计",
                "科幻未来世界，人类与AI的生存冒险，探讨科技与人性",
                "民国时期上海滩，黑帮与商战的悬疑剧本，充满时代气息",
                "现代校园青春故事，关于友情与梦想的成长历程",
                "维多利亚时代的伦敦，侦探与罪犯的智力较量",
                "末日后的废土世界，幸存者之间的信任与背叛"
        };

        for (int i = 0; i < examples.length; i++) {
            System.out.println((i + 1) + ". " + examples[i]);
        }

        System.out.println("\n💡 提示：");
        System.out.println("• 描述越详细，AI生成的标语越精准");
        System.out.println("• 可以包含时代背景、故事类型、主要冲突等信息");
        System.out.println("• 建议描述长度在20-100字之间");

        System.out.println("\n按回车键返回主菜单...");
        new Scanner(System.in).nextLine();
    }

    /**
     * Slogan输出处理器
     */
    private static class SloganOutputHandler {
        private StringBuilder fullContent = new StringBuilder();
        private int sloganCount = 0;
        private int coreIdeaCount = 0;
        private long startTime = System.currentTimeMillis();

        public void handleOutput(String content) {
            if (content == null || content.trim().isEmpty()) {
                return;
            }

            // 累加内容
            fullContent.append(content);

            // 统计标语和核心创意数量
            if (content.contains("# 标语")) {
                sloganCount++;
                System.out.print("🎯 ");
            } else if (content.contains("# 核心创意")) {
                coreIdeaCount++;
                System.out.print("💡 ");
            }

            // 输出内容
            System.out.print(content);
            System.out.flush();
        }

        public void showStatistics() {
            long duration = System.currentTimeMillis() - startTime;
            String fullText = fullContent.toString();

            System.out.println("\n📊 生成统计:");
            System.out.println("⏱️  耗时: " + duration + "ms");
            System.out.println("📝 总字符数: " + fullText.length());
            System.out.println("🎯 标语数量: " + sloganCount);
            System.out.println("💡 核心创意数量: " + coreIdeaCount);
            System.out.println("📈 平均生成速度: " +
                    String.format("%.2f", (double) fullText.length() / duration * 1000) + " 字符/秒");
        }
    }
}
