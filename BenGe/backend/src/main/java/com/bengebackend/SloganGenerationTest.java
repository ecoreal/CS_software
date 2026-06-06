package com.bengebackend;

import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.config.XfyunConfig;
import com.bengebackend.entity.Slogan;
import com.bengebackend.service.serviceImpl.AIServicelmpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 测试非流式Slogan生成功能
 */
public class SloganGenerationTest {

    public static void main(String[] args) {
        System.out.println("🚀 非流式Slogan生成测试");
        System.out.println("=".repeat(50));

        // 初始化服务
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        XfyunConfig xfyunConfig = new XfyunConfig();
        AIServicelmpl aiService = new AIServicelmpl(restTemplate, objectMapper, xfyunConfig);

        // 测试用例
        String[] testPrompts = {
                "一个发生在民国时期上海滩的悬疑剧本",
                "现代都市中的心理推理剧本",
                "古代宫廷中的权谋斗争剧本"
        };

        for (int i = 0; i < testPrompts.length; i++) {
            System.out.println("\n📝 测试案例 " + (i + 1) + ":");
            System.out.println("提示词: " + testPrompts[i]);
            System.out.println("-".repeat(50));

            try {
                // 创建请求
                SloganRequestEntity request = new SloganRequestEntity(testPrompts[i], i + 1);

                // 调用生成方法
                CompletableFuture<List<Slogan>> future = aiService.GenerateSloganAsync(request);
                List<Slogan> slogans = future.get();

                // 输出结果
                System.out.println("✅ 生成成功！共生成 " + slogans.size() + " 个标语：");
                for (int j = 0; j < slogans.size(); j++) {
                    Slogan slogan = slogans.get(j);
                    System.out.println("📌 标语 " + (j + 1) + ":");
                    System.out.println("   内容: " + slogan.getContent());
                    System.out.println("   核心创意: " + slogan.getCoreIdea());
                    System.out.println();
                }

            } catch (Exception e) {
                System.err.println("❌ 生成失败: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("=".repeat(50));
        }

        System.out.println("\n🎉 测试完成！");
    }
}
