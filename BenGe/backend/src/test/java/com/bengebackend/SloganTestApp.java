package com.bengebackend;

import com.bengebackend.entity.SloganRequestEntity;
import com.bengebackend.entity.Slogan;
import com.bengebackend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * 简单的命令行测试应用
 * 使用 --spring.profiles.active=test 参数运行
 */
@SpringBootApplication
@Profile("test")
public class SloganTestApp implements CommandLineRunner {

    @Autowired
    private AIService aiService;

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(SloganTestApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Slogan API 测试工具 ===");
        System.out.println("输入 'exit' 退出程序");
        
        while (true) {
            System.out.print("\n请输入剧本描述（或输入 'test' 运行预设测试）: ");
            String input = scanner.nextLine().trim();
            
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            
            if ("test".equalsIgnoreCase(input)) {
                runPredefinedTests();
                continue;
            }
            
            if (input.isEmpty()) {
                System.out.println("输入不能为空，请重新输入");
                continue;
            }
            
            testSloganGeneration(input);
        }
        
        scanner.close();
        System.out.println("测试结束");
        System.exit(0);
    }
    
    private void testSloganGeneration(String prompt) {
        try {
            System.out.println("\n开始生成Slogan...");
            long startTime = System.currentTimeMillis();
            
            SloganRequestEntity request = new SloganRequestEntity();
            request.setPrompt(prompt);
            request.setScriptId(1);
            
            // 测试非流式API
            CompletableFuture<List<Slogan>> future = aiService.GenerateSloganAsync(request);
            List<Slogan> slogans = future.get();
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("✅ 生成完成，耗时: " + (endTime - startTime) + "ms");
            System.out.println("📝 返回结果:");
            System.out.println("==================================");
            
            for (int i = 0; i < slogans.size(); i++) {
                Slogan slogan = slogans.get(i);
                System.out.println("🏷️  标语 " + (i + 1) + ": " + slogan.getContent());
                System.out.println("💡 核心创意: " + slogan.getCoreIdea());
                System.out.println("----------------------------------");
            }
            
            // 测试流式API
            System.out.println("\n🔄 测试流式生成...");
            aiService.GenerateSloganStreamAsync(request, content -> {
                System.out.print(content);
            }).get();
            System.out.println("\n✅ 流式生成完成");
            
        } catch (Exception e) {
            System.err.println("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void runPredefinedTests() {
        String[] testCases = {
            "古代宫廷悬疑剧本",
            "现代校园推理故事", 
            "未来科幻悬疑设定",
            "民国时期侦探故事",
            ""  // 空输入测试
        };
        
        System.out.println("\n🧪 运行预设测试用例...");
        
        for (int i = 0; i < testCases.length; i++) {
            String testCase = testCases[i];
            System.out.println("\n--- 测试用例 " + (i + 1) + " ---");
            if (testCase.isEmpty()) {
                System.out.println("输入: [空输入]");
            } else {
                System.out.println("输入: " + testCase);
            }
            
            testSloganGeneration(testCase.isEmpty() ? "" : testCase);
            
            try {
                Thread.sleep(1000); // 避免请求过于频繁
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("\n🎉 所有预设测试完成");
    }
}
