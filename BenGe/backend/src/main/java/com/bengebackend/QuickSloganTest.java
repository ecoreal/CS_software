package com.bengebackend;

import com.bengebackend.config.XfyunConfig;
import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.service.serviceImpl.AIServicelmpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 快速测试 GenerateSloganStreamAsync 方法
 * 这是一个独立的测试类，可以直接运行
 */
public class QuickSloganTest {

    public static void main(String[] args) {
        System.out.println("🚀 开始测试 GenerateSloganStreamAsync 方法");
        System.out.println("=" + "=".repeat(50));

        // 初始化服务
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        XfyunConfig xfyunConfig = new XfyunConfig();
        AIServicelmpl aiService = new AIServicelmpl(restTemplate, objectMapper, xfyunConfig);

        // 测试数据
        String[] testPrompts = {
                "一个发生在现代都市的悬疑推理剧本，涉及连环杀手和心理博弈",
                "古代宫廷斗争，充满权力与爱情的纠葛",
                "科幻未来世界，人类与AI的生存冒险"
        };

        // 逐个测试
        for (int i = 0; i < testPrompts.length; i++) {
            System.out.println("\n📝 测试案例 " + (i + 1) + ":");
            System.out.println("提示词: " + testPrompts[i]);
            System.out.println("-".repeat(50));

            SloganRequestEntity request = new SloganRequestEntity(testPrompts[i], i + 1);

            // 创建流式输出处理器
            Consumer<String> callback = new Consumer<String>() {
                @Override
                public void accept(String content) {
                    if (content != null && !content.trim().isEmpty()) {
                        System.out.print(content);
                        System.out.flush();
                    }
                }
            };

            try {
                // 调用方法
                CompletableFuture<Void> future = aiService.GenerateSloganStreamAsync(request, callback);

                // 等待完成
                future.get();

                System.out.println("\n✅ 测试案例 " + (i + 1) + " 完成");

            } catch (Exception e) {
                System.err.println("❌ 测试案例 " + (i + 1) + " 失败: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("-".repeat(50));
        }

        System.out.println("\n🎉 所有测试完成！");
    }
}
