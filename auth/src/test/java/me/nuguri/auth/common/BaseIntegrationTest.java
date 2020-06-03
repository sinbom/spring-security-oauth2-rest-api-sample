package me.nuguri.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.support.EntityInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestApplicationConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Disabled
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthServerConfigProperties properties;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected EntityInitializer entityInitializer;

    @Autowired
    protected EntityManager entityManager;

    @BeforeEach
    protected void beforeEach() {
        entityInitializer.init(entityManager);
    }

    /**
     * Password 방식 엑세스 토큰 요청 후 토큰 반환 공통 로직
     *
     * @param username 이메일
     * @param password 비밀번호
     * @return 엑세스 토큰
     * @throws Exception
     */
    protected String getAccessToken(String username, String password, String clientId, String clientSecret) throws Exception {
        return (String) new JacksonJsonParser()
                .parseMap(mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(clientId, clientSecret))
                        .param("username", username)
                        .param("password", password)
                        .param("grant_type", GrantType.PASSWORD.toString()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("access_token");
    }

}
