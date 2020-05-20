package me.nuguri.account.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.account.property.AccountServerProperties;
import me.nuguri.account.service.AccountService;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Disabled
@Transactional
@ExtendWith(MockitoExtension.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AccountServerProperties properties;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AccountService accountService;

    @Autowired
    private RemoteTokenServices remoteTokenServices;

    @Mock
    private RestTemplate restTemplate;

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

        accountService.generate(admin);
        accountService.generate(user);
    }

    /**
     * 1. 엑세스 토큰을 인증 서버에서 발급 받는 외부 API 요청 mocking (/oauth/token)
     * 2. 엑세스 토큰 유효 여부를 인증 서버에 외부 API 요청 mocking (/oauth/check_token)
     * @param httpStatus HttpStatus.ok 인 경우 1,2번(성공) mocking 그 외의 경우 2번(실패) mocking
     * @param account check token endpoint 응답 결과 mocking 계정 정보
     */
    protected void mockRestTemplate(HttpStatus httpStatus, Account account) {
        remoteTokenServices.setRestTemplate(restTemplate);
        OngoingStubbing<ResponseEntity<Map>> when = when(restTemplate.exchange(eq(properties.getCheckTokenUrl()),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class), any(Object.class)));
        if (HttpStatus.OK.equals(httpStatus)) {
            Map<String, Object> map = new HashMap<>();
            map.put("aud", Arrays.asList("account"));
            map.put("user_name", account.getEmail());
            map.put("scope", Arrays.asList("read", "write"));
            map.put("active", true);
            map.put("id", account.getId());
            map.put("exp", 1589456203);
            map.put("authorities", account.getRoles().stream().map(r -> "ROLE_" + r).collect(Collectors.toList()));
            map.put("client_id", "mock_client_id");
            when.thenReturn(ResponseEntity.ok(map));
            when(restTemplate.exchange(eq(properties.getAccessTokenUrl()), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("{\"access_token\":\"" + UUID.randomUUID().toString() + "\"}"));
        } else {
            when.thenThrow(InvalidTokenException.class);
        }
    }

    /**
     * client_credentials 방식 엑세스 토큰 발급 요청 외부 API 호출
     * @return 엑세스 토큰
     */
    protected String getAccessToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return requestAccessToken(map);
    }

    /**
     * Password 방식 엑세스 토큰 발급 요청 외부 API 호출
     * @param email 이메일
     * @param password 비밀번호
     * @return 엑세스 토큰
     */
    protected String getAccessToken(String email, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", email);
        map.add("password", password);
        return requestAccessToken(map);
    }

    /**
     * 엑세스 토큰 발급 요청 외부 API 호출
     * @param map 요청 파라미터
     * @return 엑세스 토큰
     */
    private String requestAccessToken(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        return (String) new JacksonJsonParser()
                .parseMap(restTemplate.exchange(properties.getAccessTokenUrl(), HttpMethod.POST, httpEntity, String.class).getBody())
                .get("access_token");
    }

}
