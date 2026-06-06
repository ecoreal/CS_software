// package com.bengebackend;

// import com.bengebackend.entity.SloganRequestEntity;
// import com.bengebackend.entity.Slogan;
// import com.bengebackend.service.AIService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @AutoConfigureTestMvc
// public class SloganAPITest {

// @Autowired
// private MockMvc mockMvc;

// @Autowired
// private ObjectMapper objectMapper;

// @Autowired
// private AIService aiService;

// /**
// * 测试非流式Slogan生成API
// */
// @Test
// public void testGenerateSloganAPI() throws Exception {
// // 准备测试数据
// SloganRequestEntity request = new SloganRequestEntity();
// request.setPrompt("悬疑推理剧本，包含多个角色，背景设定在古代宫廷");
// request.setScriptId(1);

// String requestJson = objectMapper.writeValueAsString(request);

// // 执行API调用
// MvcResult result = mockMvc.perform(post("/api/ai/slogan/generate")
// .contentType(MediaType.APPLICATION_JSON)
// .content(requestJson))
// .andExpect(status().isOk())
// .andExpect(content().contentType(MediaType.APPLICATION_JSON))
// .andReturn();

// // 解析返回结果
// String responseContent = result.getResponse().getContentAsString();
// System.out.println("API返回结果: " + responseContent);

// // 验证返回格式
// Slogan[] slogans = objectMapper.readValue(responseContent, Slogan[].class);

// // 断言测试
// assertNotNull(slogans, "返回的slogans数组不应为null");
// assertEquals(3, slogans.length, "应该返回3个Slogan对象");

// for (int i = 0; i < slogans.length; i++) {
// Slogan slogan = slogans[i];
// assertNotNull(slogan.getContent(), "第" + (i+1) + "个标语内容不应为null");
// assertNotNull(slogan.getCoreIdea(), "第" + (i+1) + "个核心创意不应为null");
// assertFalse(slogan.getContent().trim().isEmpty(), "第" + (i+1) + "个标语内容不应为空");
// assertFalse(slogan.getCoreIdea().trim().isEmpty(), "第" + (i+1) +
// "个核心创意不应为空");

// System.out.println("标语 " + (i+1) + ": " + slogan.getContent());
// System.out.println("核心创意 " + (i+1) + ": " + slogan.getCoreIdea());
// System.out.println("---");
// }
// }

// /**
// * 测试空输入的情况
// */
// @Test
// public void testGenerateSloganWithEmptyPrompt() throws Exception {
// SloganRequestEntity request = new SloganRequestEntity();
// request.setPrompt("");
// request.setScriptId(1);

// String requestJson = objectMapper.writeValueAsString(request);

// MvcResult result = mockMvc.perform(post("/api/ai/slogan/generate")
// .contentType(MediaType.APPLICATION_JSON)
// .content(requestJson))
// .andExpect(status().isOk())
// .andReturn();

// String responseContent = result.getResponse().getContentAsString();
// Slogan[] slogans = objectMapper.readValue(responseContent, Slogan[].class);

// // 即使是空输入，也应该返回默认的3个标语
// assertEquals(3, slogans.length, "空输入也应该返回3个默认标语");
// System.out.println("空输入测试结果: " + responseContent);
// }

// /**
// * 直接测试AI服务层
// */
// @Test
// public void testAIServiceDirectly() throws Exception {
// SloganRequestEntity request = new SloganRequestEntity();
// request.setPrompt("现代都市悬疑剧本，涉及商业阴谋");

// // 测试非流式方法
// CompletableFuture<List<Slogan>> future =
// aiService.GenerateSloganAsync(request);
// List<Slogan> slogans = future.get();

// assertNotNull(slogans, "AI服务返回的slogans不应为null");
// assertEquals(3, slogans.size(), "应该返回3个Slogan对象");

// System.out.println("AI服务直接测试结果:");
// for (int i = 0; i < slogans.size(); i++) {
// Slogan slogan = slogans.get(i);
// System.out.println("标语 " + (i+1) + ": " + slogan.getContent());
// System.out.println("核心创意 " + (i+1) + ": " + slogan.getCoreIdea());
// System.out.println("---");
// }
// }

// /**
// * 测试多种不同的输入
// */
// @Test
// public void testVariousPrompts() throws Exception {
// String[] testPrompts = {
// "古代宫廷悬疑剧本",
// "现代校园推理故事",
// "未来科幻悬疑设定",
// "民国时期侦探故事",
// "恐怖悬疑剧本"
// };

// for (String prompt : testPrompts) {
// SloganRequestEntity request = new SloganRequestEntity();
// request.setPrompt(prompt);

// String requestJson = objectMapper.writeValueAsString(request);

// MvcResult result = mockMvc.perform(post("/api/ai/slogan/generate")
// .contentType(MediaType.APPLICATION_JSON)
// .content(requestJson))
// .andExpect(status().isOk())
// .andReturn();

// String responseContent = result.getResponse().getContentAsString();
// Slogan[] slogans = objectMapper.readValue(responseContent, Slogan[].class);

// System.out.println("测试输入: " + prompt);
// System.out.println("返回结果: " + responseContent);
// System.out.println("========================================");

// assertEquals(3, slogans.length, "输入'" + prompt + "'应该返回3个标语");
// }
// }
// }
