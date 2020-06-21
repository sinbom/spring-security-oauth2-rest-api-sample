package me.nuguri.account.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.account.property.AccountServerProperties;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.account.service.AccountService;
import me.nuguri.common.adapter.CustomUserAuthentication;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Scopes;
import me.nuguri.common.support.EntityInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected AccountRepository accountRepository;

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
     *
     * @param httpStatus HttpStatus.ok 인 경우 1,2번(성공) mocking 그 외의 경우 2번(실패) mocking
     * @param email      mocking 할 계정의 이메일 정보
     */
    protected void mockRestTemplate(HttpStatus httpStatus, String email) {
        if (HttpStatus.OK.equals(httpStatus)) {
            Account account = accountRepository.findByEmail(email).orElseThrow(EntityExistsException::new);
            Long id = account.getId();
            when(
                    restTemplate.exchange(
                            eq(properties.getAccessTokenUrl()),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)
                    )
            ).thenReturn(ResponseEntity.ok("{\"access_token\":\"mockedAccessToken\"}"));
            Map<String, String> params = new HashMap<>();
            params.put("client_id", "nuguri");
            Set<String> scopes = new HashSet<>(Arrays.asList(Scopes.READ.toString(), Scopes.WRITE.toString()));
            Collection<? extends GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_" + account.getRoles()));
            UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(account.getEmail(), "N/A", authorities);
            CustomUserAuthentication mockUser = new CustomUserAuthentication(user, id);
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
