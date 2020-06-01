package me.nuguri.account.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.account.property.AccountServerProperties;
import me.nuguri.account.service.AccountService;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import me.nuguri.common.initializer.EntityInitializer;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import redis.embedded.RedisServer;

import javax.persistence.EntityManager;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestApplicationConfiguration.class)
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
    protected EntityInitializer entityInitializer;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    @Mock
    private TokenStore mockTokenStore;

    @Mock
    private OAuth2AccessToken mockAccessToken;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    protected void beforeEach() {
        entityInitializer.init(entityManager);
    }

    /**
     * 1. JWT 토큰을 인증 서버에서 발급 받는 외부 API 요청 mocking (/oauth/token)
     * 2. JWT 토큰 유효 여부 검사 로직을 담당하는 TokenStore 및 결과 AccessToken mocking
     * @param httpStatus HttpStatus.ok 인 경우 1,2번(성공) mocking 그 외의 경우 2번(실패) mocking
     * @param account mocking 할 계정 정보
     */
    protected void mockRestTemplate(HttpStatus httpStatus, Account account) {
        if (HttpStatus.OK.equals(httpStatus)) {
            when(restTemplate.exchange(
                    eq(properties.getAccessTokenUrl()),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(String.class)
                    )
            ).thenReturn(ResponseEntity.ok(
                    "{\"access_token\":\""
                            + "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibnVndXJpIiwiYWNjb3VudCJdLCJ1c2VyX25hbWUiOiJhZG1pbkBuYXZlci5jb20iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiZXhwIjoxNTkwOTMxNDU5LCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjlhNjE1OGJhLWZjNTAtNDE1MC1iMTlhLTAxYjg2ODNlODlhMCIsImNsaWVudF9pZCI6Im51Z3VyaSJ9.MJvod1EllyirjqYTatcfkv2xuYRBnK56HVgKKqraa7oSmOvn46FMe_UtnyIp55BL3mgX1KAGecwjqkLFRjan2QVMmoGX81aouqMzbbJj4diqQtHbRqxwduP8Cby9WNu6sUlIgL1UBhVgFWKD7dDNvvft8I95qXcVDy9xy3LLj9vJDNZuDl2ym7gNVTEp3Aa5X-ZV7MjcIliOXfCDJbng40qj7VGVpm8FkJ_LsL_XD_XV0kiPqYeXqY7bpv8nE6SbP7fL4A-GiqZa5wCHXTk1hLclH0Gpd7w7GLkr-gJYR9sYrupXvPeuNzjyNE3JKhj8BYwdZlajm0vkP8LLBke0rQ"
                            + "\"}")
            );
            Map<String, String> params = new HashMap<>();
            params.put("client_id", "nuguri");
            Set<String> scopes = new HashSet<>(Arrays.asList(Scope.READ.toString(), Scope.WRITE.toString()));
            Collection<? extends GrantedAuthority> authorities = account
                    .getRoles()
                    .stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(toList());
            UsernamePasswordAuthenticationToken mockUser = new UsernamePasswordAuthenticationToken(account.getEmail(), "N/A", authorities);
            Set<String> resourceIds = new HashSet<>(Arrays.asList("account", "nuguri"));
            OAuth2Request mockRequest = new OAuth2Request(params, "nuguri", authorities, true,
                    scopes, resourceIds, null, null, null);
            OAuth2Authentication mockAuthentication = new OAuth2Authentication(mockRequest, mockUser);
            when(mockTokenStore.readAccessToken(any())).thenReturn(mockAccessToken);
            when(mockAccessToken.isExpired()).thenReturn(false);
            when(mockTokenStore.readAuthentication(mockAccessToken)).thenReturn(mockAuthentication);
        } else {
            when(mockTokenStore.readAccessToken(any())).thenReturn(null);
        }
        defaultTokenServices.setTokenStore(mockTokenStore);
    }

    /**
     * client_credentials 방식 엑세스 토큰 발급 요청 외부 API 호출
     *
     * @return 엑세스 토큰
     */
    protected String getAccessToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return requestAccessToken(map);
    }

    /**
     * Password 방식 엑세스 토큰 발급 요청 외부 API 호출
     *
     * @param email    이메일
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
     *
     * @param map 요청 파라미터
     * @return 엑세스 토큰
     */
    private String requestAccessToken(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                properties.getAccessTokenUrl(),
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        JacksonJsonParser parser = new JacksonJsonParser();
        return (String) parser.parseMap(response.getBody()).get("access_token");
    }

}
