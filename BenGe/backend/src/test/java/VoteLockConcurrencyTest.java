import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest(classes = com.bengebackend.BengeBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VoteLockConcurrencyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    static class TestWebSocketClient extends WebSocketClient {
        private final CountDownLatch readyLatch;
        private final String authMessage;
        private final String voteMessage;

        public TestWebSocketClient(URI serverUri, CountDownLatch readyLatch, String authMessage, String voteMessage) {
            super(serverUri);
            this.readyLatch = readyLatch;
            this.authMessage = authMessage;
            this.voteMessage = voteMessage;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            System.out.println("连接建立： " + getURI());
            send(authMessage);
        }

        @Override
        public void onMessage(String message) {
            System.out.println("收到消息：" + message + " 时间：" + System.currentTimeMillis());
            try {
                JsonNode node = new ObjectMapper().readTree(message);
                if ("userInfo".equalsIgnoreCase(node.get("type").asText())) {
                    System.out.println("一个用户进入房间");
                    readyLatch.countDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (message.contains("\"type\":\"vote\"")) {
                System.out.println("收到投票结果 from 用户的消息时间：" + System.currentTimeMillis());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("连接关闭：" + reason);
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }

        public void sendVoteMessage() {
            if (this.isOpen()) {
                send(voteMessage);
            } else {
                System.err.println("连接未打开，无法发送投票消息");
            }
        }
    }

    @Test
    public void testConcurrentVoteWithLock() throws Exception {
        String url = "ws://localhost:" + port + "/ws";

        CountDownLatch latch = new CountDownLatch(2);
        String roomId = "1";

        String auth1 = objectMapper.writeValueAsString(Map.of(
                "type", "auth",
                "token",
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5IiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlhdCI6MTc1MTUyNjMyOSwiZXhwIjoxNzUxNjEyNzI5fQ.s2nb44azTYUVPAqAwy-w8T1DtqIDjbTVu52X_CMSnsk",
                "roomId", roomId,
                "avatar", "avatar1"));

        String auth2 = objectMapper.writeValueAsString(Map.of(
                "type", "auth",
                "token",
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMSIsInVzZXJuYW1lIjoidGVzdHVzZXIxMjMiLCJpYXQiOjE3NTE1MjcxMjQsImV4cCI6MTc1MTYxMzUyNH0.6kQ_ywtUWf8wjAp14uzSftqRK6GpolhWpuPSvQtNj1M",
                "roomId", roomId,
                "avatar", "avatar2"));

        String voteMessage1 = objectMapper.writeValueAsString(Map.of(
                "type", "vote",
                "roomId", roomId,
                "key", List.of("item1"),
                "vote", List.of(1),
                "hasVoted", true,
                "hasChosen", true));

        String voteMessage2 = objectMapper.writeValueAsString(Map.of(
                "type", "vote",
                "roomId", roomId,
                "key", List.of("item1"),
                "vote", List.of(2),
                "hasVoted", true,
                "hasChosen", true));

        TestWebSocketClient client1 = new TestWebSocketClient(new URI(url), latch, auth1, voteMessage1);
        TestWebSocketClient client2 = new TestWebSocketClient(new URI(url), latch, auth2, voteMessage2);

        client1.connectBlocking();
        client2.connectBlocking();

        latch.await(); // 等待所有客户端连接完成

        // 几乎同时发送投票消息
        Thread t1 = new Thread(client1::sendVoteMessage);
        Thread t2 = new Thread(client2::sendVoteMessage);

        long start = System.currentTimeMillis();

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Thread.sleep(3000);
        long end = System.currentTimeMillis();

        System.out.println("耗时：" + (end - start) + "毫秒");

        client1.close();
        client2.close();
    }
}
