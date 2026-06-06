// package com.bengebackend;

// import com.bengebackend.entity.SloganRequestEntity;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.TimeUnit;

// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureTestMvc
// public class SloganStreamAPITest {

// @Autowired
// private MockMvc mockMvc;

// @Autowired
// private ObjectMapper objectMapper;

// /**
// * 测试流式Slogan生成API
// * 注意：这个测试会实际调用AI服务，需要确保网络连接正常
// */
// @Test
// public void testGenerateSloganStreamAPI() throws Exception {
// SloganRequestEntity request = new SloganRequestEntity();
// request.setPrompt("悬疑推理剧本，背景设定在古代宫廷");
// request.setScriptId(1);

// String requestJson = objectMapper.writeValueAsString(request);

// // 执行流式API调用
// MvcResult result = mockMvc.perform(post("/api/ai/slogan/stream")
// .contentType(MediaType.APPLICATION_JSON)
// .content(requestJson))
// .andExpect(status().isOk())
// .andExpect(header().string("Content-Type",
// "text/event-stream;charset=UTF-8"))
// .andReturn();

// // 注意：在实际测试中，流式响应需要异步处理
// String responseContent = result.getResponse().getContentAsString();
// System.out.println("流式API返回内容: " + responseContent);
// }

// /**
// * 使用实际的HTTP客户端测试流式API
// */
// @Test
// public void testStreamAPIWithRealClient() throws Exception {
// // 这个测试需要启动实际的服务器
// // 可以使用@SpringBootTest(webEnvironment =
// SpringBootTest.WebEnvironment.RANDOM_PORT)
// System.out.println("此测试需要启动实际服务器进行验证");
// }
// }
