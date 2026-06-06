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
 * Slogan流式生成测试命令行工具
 * 用于测试 GenerateSloganStreamAsync 方法
 */
public class SloganStreamTestCommand {

    private static AIServicelmpl aiService;

    public static void main(String[] args) {
        // 初始化服务
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        XfyunConfig xfyunConfig = new XfyunConfig();
        aiService = new AIServicelmpl(restTemplate, objectMapper, xfyunConfig);

        System.out.println("=================================");
        System.out.println("  Slogan流式生成测试工具");
        System.out.println("=================================");
        System.out.println("输入 'exit' 退出程序");
        System.out.println("输入 'help' 查看帮助信息");
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("请输入剧本描述 > ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("再见！");
                break;
            }

            if ("help".equalsIgnoreCase(input)) {
                showHelp();
                continue;
            }

            if (input.isEmpty()) {
                System.out.println("请输入有效的剧本描述！");
                continue;
            }

            // 执行测试
            testSloganGeneration(input);
        }

        scanner.close();
    }

    /**
     * 测试Slogan生成
     */
    private static void testSloganGeneration(String prompt) {
        System.out.println("\n开始生成Slogan...");
        System.out.println("提示词: " + prompt);
        System.out.println("----------------------------------------");

        // 创建请求实体
        SloganRequestEntity request = new SloganRequestEntity(prompt, 1);

        // 创建回调函数处理流式输出
        Consumer<String> callback = new Consumer<String>() {
            private StringBuilder fullContent = new StringBuilder();

            @Override
            public void accept(String content) {
                if (content != null && !content.trim().isEmpty()) {
                    // 实时显示流式内容
                    System.out.print(content);
                    fullContent.append(content);

                    // 如果内容包含换行符，刷新输出
                    if (content.contains("\n")) {
                        System.out.flush();
                    }
                }
            }
        };

        try {
            // 调用流式生成方法
            CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);

            // 等待完成
            future.get();

            System.out.println("\n----------------------------------------");
            System.out.println("✅ Slogan生成完成！");

        } catch (Exception e) {
            System.err.println("\n❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n按回车继续...");
        try (Scanner tempScanner = new Scanner(System.in)) {
            tempScanner.nextLine();
        }
    }

    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("\n=== 帮助信息 ===");
        System.out.println("这是一个测试 GenerateSloganStreamAsync 方法的命令行工具。");
        System.out.println();
        System.out.println("使用方法:");
        System.out.println("1. 输入剧本描述，例如：");
        System.out.println("   - 一个发生在现代都市的悬疑推理剧本");
        System.out.println("   - 古代宫廷斗争，涉及权力与爱情");
        System.out.println("   - 科幻未来世界的生存冒险");
        System.out.println();
        System.out.println("2. 程序会调用AI流式生成3个不同风格的标语");
        System.out.println("3. 生成的内容会实时显示在控制台");
        System.out.println();
        System.out.println("测试示例:");
        System.out.println("输入: 一个发生在民国时期上海滩的悬疑剧本");
        System.out.println("输出: 会生成3个标语及其核心创意");
        System.out.println();
        System.out.println("命令:");
        System.out.println("  help  - 显示帮助信息");
        System.out.println("  exit  - 退出程序");
        System.out.println("===================\n");
    }
}
