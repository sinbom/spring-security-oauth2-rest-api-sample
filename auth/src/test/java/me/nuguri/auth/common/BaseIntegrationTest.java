package me.nuguri.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.auth.service.AccountService;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Disabled
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthServerConfigProperties properties;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AccountService accountService;

    protected void generateTestEntities() {
        Account admin = new Account();
        admin.setName("관리자");
        admin.setEmail(properties.getAdminEmail());
        admin.setPassword(properties.getAdminPassword());
        admin.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)));

        Account user = new Account();
        user.setName("사용자");
        user.setEmail(properties.getUserEmail());
        user.setPassword(properties.getUserPassword());
        user.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

        Client client = new Client();
        client.setClientId(properties.getClientId());
        client.setClientSecret(passwordEncoder.encode(properties.getClientSecret()));
        client.setResourceIds("account");
        client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
        client.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()));
        client.setRedirectUri(properties.getRedirectUri());
        client.setAuthorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()));
        client.addAccount(admin);

        Client client2 = new Client();
        client2.setClientId("test");
        client2.setClientSecret(passwordEncoder.encode("test"));
        client2.setResourceIds("account");
        client2.setScope(String.join(",", Scope.READ.toString()));
        client2.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.CLIENT_CREDENTIALS.toString()));
        client2.setRedirectUri(properties.getRedirectUri());
        client2.setAuthorities(String.join(",", Role.USER.toString()));
        client2.addAccount(user);

        accountService.generate(admin);
        accountService.generate(user);
    }

    /**
     * Password 방식 엑세스 토큰 요청 후 토큰 반환 공통 로직
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
