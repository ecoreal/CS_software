
import com.bengebackend.entity.UserRegisterEntity;
import com.bengebackend.controller.UserController.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(classes = com.bengebackend.BengeBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserAuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRegisterAndLogin() {
        // 1. 注册用户
        UserRegisterEntity registerRequest = new UserRegisterEntity();
        registerRequest.setUsername("testuser123");
        registerRequest.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserRegisterEntity> registerEntity = new HttpEntity<>(registerRequest, headers);
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/user/register", registerEntity, String.class);

        Assertions.assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        Assertions.assertEquals("注册成功", registerResponse.getBody());

        // 2. 登录获取token
        // 注意：login接口是用@RequestParam，所以要用URL参数形式传递
        String loginUrl = "/api/user/login?username=testuser123&password=123456";
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(loginUrl, null, LoginResponse.class);

        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        Assertions.assertNotNull(loginResponse.getBody());
        Assertions.assertNotNull(loginResponse.getBody().getToken());

        System.out.println("登录成功，token: " + loginResponse.getBody().getToken());
    }
}
