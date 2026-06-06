package com.bengebackend;

import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * GenerateSloganStreamAsync 方法的 Spring Boot 测试类
 */
@SpringBootTest
public class GenerateSloganStreamAsyncTest {

    @Autowired
    private AIService aiService;

    /**
     * 测试基本的Slogan流式生成功能
     */
    @Test
    public void testBasicSloganGeneration() throws Exception {
        System.out.println("=== 测试基本Slogan生成 ===");

        // 准备测试数据
        SloganRequestEntity request = new SloganRequestEntity(
                "一个发生在现代都市的悬疑推理剧本，涉及连环杀手和心理博弈",
                1);

        // 用于等待异步完成
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder result = new StringBuilder();

        // 创建回调函数
        Consumer<String> callback = content -> {
            if (content != null && !content.trim().isEmpty()) {
                System.out.print(content);
                result.append(content);
                System.out.flush();
            }
        };

        // 调用流式生成方法
        CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);

        // 添加完成回调
        future.whenComplete((v, throwable) -> {
            if (throwable != null) {
                System.err.println("生成失败: " + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                System.out.println("\n✅ 生成完成！");
            }
            latch.countDown();
        });

        // 等待完成，最多等待120秒
        boolean completed = latch.await(120, TimeUnit.SECONDS);
        if (!completed) {
            System.err.println("❌ 测试超时");
        }

        // 验证结果
        String finalResult = result.toString();
        System.out.println("\n=== 生成结果 ===");
        System.out.println("总字符数: " + finalResult.length());
        System.out.println("是否包含标语: " + finalResult.contains("标语"));
        System.out.println("是否包含核心创意: " + finalResult.contains("核心创意"));
    }

    /**
     * 测试不同类型的剧本描述
     */
    @Test
    public void testDifferentGenres() throws Exception {
        System.out.println("=== 测试不同类型剧本 ===");

        String[] testPrompts = {
                "古代宫廷斗争，涉及权力与爱情的纠葛",
                "科幻未来世界，人类与AI的生存冒险",
                "现代校园青春，关于友情与梦想的故事",
                "民国时期上海滩，黑帮与商战的悬疑剧本"
        };

        for (int i = 0; i < testPrompts.length; i++) {
            System.out.println("\n--- 测试案例 " + (i + 1) + " ---");
            System.out.println("提示词: " + testPrompts[i]);
            System.out.println("生成内容:");

            SloganRequestEntity request = new SloganRequestEntity(testPrompts[i], i + 1);
            CountDownLatch latch = new CountDownLatch(1);

            Consumer<String> callback = content -> {
                if (content != null && !content.trim().isEmpty()) {
                    System.out.print(content);
                    System.out.flush();
                }
            };

            CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);
            future.whenComplete((v, throwable) -> {
                if (throwable != null) {
                    System.err.println("生成失败: " + throwable.getMessage());
                }
                latch.countDown();
            });

            // 等待完成
            latch.await(60, TimeUnit.SECONDS);
            System.out.println("\n");
        }
    }

    /**
     * 测试空输入和边界情况
     */
    @Test
    public void testEdgeCases() throws Exception {
        System.out.println("=== 测试边界情况 ===");

        // 测试空输入
        testSingleCase("", "空输入测试");

        // 测试很短的输入
        testSingleCase("剧本", "短输入测试");

        // 测试很长的输入
        String longInput = "这是一个非常详细的剧本描述，包含了大量的背景信息、人物设定、情节安排等内容。".repeat(10);
        testSingleCase(longInput, "长输入测试");

        // 测试特殊字符
        testSingleCase("包含特殊字符的剧本：@#$%^&*()，测试系统的处理能力", "特殊字符测试");
    }

    /**
     * 辅助方法：测试单个案例
     */
    private void testSingleCase(String prompt, String testName) throws Exception {
        System.out.println("\n--- " + testName + " ---");
        System.out.println("输入: " + (prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt));

        SloganRequestEntity request = new SloganRequestEntity(prompt, 999);
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<String> callback = content -> {
            if (content != null && !content.trim().isEmpty()) {
                System.out.print(content);
                System.out.flush();
            }
        };

        try {
            CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);
            future.whenComplete((v, throwable) -> {
                if (throwable != null) {
                    System.err.println("生成失败: " + throwable.getMessage());
                }
                latch.countDown();
            });

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("测试超时");
            }
        } catch (Exception e) {
            System.err.println("异常: " + e.getMessage());
        }
    }

    /**
     * 测试并发调用
     */
    @Test
    public void testConcurrentCalls() throws Exception {
        System.out.println("=== 测试并发调用 ===");

        int concurrentCount = 3;
        CountDownLatch latch = new CountDownLatch(concurrentCount);

        for (int i = 0; i < concurrentCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    SloganRequestEntity request = new SloganRequestEntity(
                            "并发测试剧本 " + index + "：现代都市悬疑推理",
                            index);

                    Consumer<String> callback = content -> {
                        if (content != null && !content.trim().isEmpty()) {
                            System.out.print("[线程" + index + "] " + content);
                            System.out.flush();
                        }
                    };

                    CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);
                    future.get(60, TimeUnit.SECONDS);

                } catch (Exception e) {
                    System.err.println("线程 " + index + " 异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        // 等待所有线程完成
        latch.await(180, TimeUnit.SECONDS);
        System.out.println("\n✅ 并发测试完成");
    }
}
