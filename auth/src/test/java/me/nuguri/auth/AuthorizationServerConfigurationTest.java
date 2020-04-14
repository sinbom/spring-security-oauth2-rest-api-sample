package me.nuguri.auth;

import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.common.GrantType;
import me.nuguri.auth.common.Role;
import me.nuguri.auth.config.RestDocsConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class AuthorizationServerConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthServerConfigProperties authServerConfigProperties;

    @Autowired
    private TokenStore tokenStore;

    /**
     * 엑세스 토큰 스토어 초기화
     */
    @After
    public void clearTokenStore() {
        InMemoryTokenStore tokenStore = (InMemoryTokenStore) this.tokenStore;
        tokenStore.clear();
    }

    /**
     * 인증 서버 엑세스 토큰 유효한 경우
     * @throws Exception
     */
    @Test
    public void checkAccessToken_Success_200() throws Exception {
        String access_token = (String) new JacksonJsonParser().parseMap(getAccessTokenPasswordGrantTypeResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("access_token");

        mockMvc.perform(post("/oauth/check_token")
                .param("token", access_token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active").exists())
                .andExpect(jsonPath("active").value(true))
                .andExpect(jsonPath("exp").exists())
                .andExpect(jsonPath("user_name").exists())
                .andExpect(jsonPath("authorities").exists())
                .andExpect(jsonPath("client_id").exists())
                .andExpect(jsonPath("scope").exists())
                .andDo(print())
                .andDo(document("check-access_token",
                        requestParameters(
                                parameterWithName("token").description("access token")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CACHE_CONTROL).description("cache control"),
                                headerWithName(HttpHeaders.PRAGMA).description("pragma"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName("X-Content-Type-Options").description("X-Content-Type-Options"),
                                headerWithName("X-XSS-Protection").description("X-XSS-Protection"),
                                headerWithName("X-Frame-Options").description("X-Frame-Options")
                        ),
                        responseFields(
                                fieldWithPath("active").description("access token active"),
                                fieldWithPath("exp").description("access token expires"),
                                fieldWithPath("user_name").description("username"),
                                fieldWithPath("authorities").description("authorities"),
                                fieldWithPath("client_id").description("client_id"),
                                fieldWithPath("scope").description("access scopes")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰 유효하지 않는 경우
     * @throws Exception
     */
    @Test
    public void checkAccessToken_Invalid_AccessToken_400() throws Exception {
        String access_token = (String) new JacksonJsonParser().parseMap(getAccessTokenPasswordGrantTypeResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("access_token");

        mockMvc.perform(post("/oauth/revoke_token")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, access_token));

        mockMvc.perform(post("/oauth/check_token")
                .param("token", access_token))
                .andExpect(status().isBadRequest());
    }

    /**
     * 인증 서버 엑세스 토큰 Authorization Code 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Authorization_Code_Success_200() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(get("/oauth/authorize")
                .session(new MockHttpSession())
                .with(user(authServerConfigProperties.getAdminEmail()).password(authServerConfigProperties.getAdminPassword()).roles(Role.ADMIN.name()))
                .param("response_type", "code")
                .param("client_id", authServerConfigProperties.getClientId())
                .param("redirect_uri", authServerConfigProperties.getRedirectUri())
                .param("scope", "read")).andDo(print()).andReturn().getRequest().getSession();

        String redirectedUri = mockMvc.perform(post("/oauth/authorize")
                .with(csrf())
                .session(session)
                .param("response_type", "code")
                .param("client_id", authServerConfigProperties.getClientId())
                .param("redirect_uri", authServerConfigProperties.getRedirectUri())
                .param("scope", "read")
                .param("scope.read", "true")
                .param("user_oauth_approval", "true"))
                .andDo(print()).andReturn().getResponse().getRedirectedUrl();

        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("code", redirectedUri.substring(redirectedUri.lastIndexOf("=") + 1))
                .param("grant_type", GrantType.AUTHORIZATION_CODE.toString())
                .param("redirect_uri", authServerConfigProperties.getRedirectUri()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists()
        );
    }

    /**
     * 인증 서버 엑세스 토큰 Implicit 방식으로 정상적으로 얻는 경우
     */
    @Test
    public void getAccessToken_GrantType_Implicit_Success_200() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(get("/oauth/authorize")
                .session(new MockHttpSession())
                .with(user(authServerConfigProperties.getAdminEmail()).password(authServerConfigProperties.getAdminPassword()).roles(Role.ADMIN.name()))
                .param("response_type", "token")
                .param("client_id", authServerConfigProperties.getClientId())
                .param("redirect_uri", authServerConfigProperties.getRedirectUri())
                .param("scope", "read")).andDo(print()).andReturn().getRequest().getSession();

        String redirectedUri = mockMvc.perform(post("/oauth/authorize")
                .with(csrf())
                .session(session)
                .param("response_type", "token")
                .param("client_id", authServerConfigProperties.getClientId())
                .param("redirect_uri", authServerConfigProperties.getRedirectUri())
                .param("scope", "read")
                .param("scope.read", "true")
                .param("user_oauth_approval", "true"))
                .andExpect(status().is3xxRedirection())
                .andDo(print())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUri).contains("access_token");
        assertThat(redirectedUri).contains("token_type");
        assertThat(redirectedUri).contains("expires_in");

    }

    /**
     * 인증 서버 엑세스 토큰 Password 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Password_Success_200() throws Exception {
        getAccessTokenPasswordGrantTypeResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andDo(print())
                .andDo(document("get-access_token-password-grantType",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Basic [clientId:clientSecret](Base64 Encoding Value)")
                        ),
                        requestParameters(
                                parameterWithName("username").description("user account email"),
                                parameterWithName("password").description("user account password"),
                                parameterWithName("scope").description("access token scope").optional(),
                                parameterWithName("grant_type").description("access token grant type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CACHE_CONTROL).description("cache control"),
                                headerWithName(HttpHeaders.PRAGMA).description("pragma"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName("X-Content-Type-Options").description("X-Content-Type-Options"),
                                headerWithName("X-XSS-Protection").description("X-XSS-Protection"),
                                headerWithName("X-Frame-Options").description("X-Frame-Options")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("access token"),
                                fieldWithPath("token_type").description("access token type"),
                                fieldWithPath("refresh_token").description("refresh token"),
                                fieldWithPath("expires_in").description("access token expires time"),
                                fieldWithPath("scope").description("access scopes")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰 Password 방식으로 HttpBasic 헤더 값 없어서 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Password_No_HttpBasic_401() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .param("username", authServerConfigProperties.getAdminEmail())
                .param("password", authServerConfigProperties.getAdminPassword())
                .param("grant_type", GrantType.PASSWORD.toString()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    /**
     * 인증 서버 엑세스 토큰 Password 방식으로 부정확한 username 및 password 입력으로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Password_Invalid_Username_400() throws Exception {
        getAccessTokenPasswordGrantTypeResponse("noexistemail@test.com", "12341234")
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    /**
     * 인증 서버 엑세스 토큰 Refresh Token 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_RefreshToken_Success_200() throws Exception {
        String refresh_token = (String) new JacksonJsonParser()
                .parseMap(getAccessTokenPasswordGrantTypeResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("refresh_token");

        getAccessTokenRefreshTokenGrantTypeResponse(refresh_token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andDo(print())
                .andDo(document("get-access_token-refresh_token-grantType",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Basic [clientId:clientSecret](Base64 Encoding Value)")
                        ),
                        requestParameters(
                                parameterWithName("refresh_token").description("refresh token"),
                                parameterWithName("scope").description("access token scope").optional(),
                                parameterWithName("grant_type").description("access token grant type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CACHE_CONTROL).description("cache control"),
                                headerWithName(HttpHeaders.PRAGMA).description("pragma"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName("X-Content-Type-Options").description("X-Content-Type-Options"),
                                headerWithName("X-XSS-Protection").description("X-XSS-Protection"),
                                headerWithName("X-Frame-Options").description("X-Frame-Options")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("access token"),
                                fieldWithPath("token_type").description("access token type"),
                                fieldWithPath("refresh_token").description("refresh token"),
                                fieldWithPath("expires_in").description("access token expires time"),
                                fieldWithPath("scope").description("access scopes")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰 Refresh Token 방식으로 HttpBasic 헤더 값 없어서 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_RefreshToken_No_HttpBasic_401() throws Exception {
        String refresh_token = (String) new JacksonJsonParser()
                .parseMap(getAccessTokenPasswordGrantTypeResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("refresh_token");

        mockMvc.perform(post("/oauth/token")
                .param("refresh_token", refresh_token)
                .param("grant_type", GrantType.REFRESH_TOKEN.toString()))
                .andExpect(status().isUnauthorized())
                .andDo(print())
        ;
    }

    /**
     * 인증 서버 엑세스 토큰 Refresh Token 방식으로 부정확한 refresh token 입력으로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_RefreshToken_Invalid_Token_400() throws Exception {
        getAccessTokenRefreshTokenGrantTypeResponse("invalid_token!@(#*&!@(*#")
                .andExpect(status().isBadRequest())
                .andDo(print())
        ;
    }

    /**
     * 인증 서버 엑세스 토큰 Client Credentials 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_ClientCredentials_Success_200() throws Exception {
        getAccessTokenClientCredentialsGrantTypeResponse(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andDo(print())
                .andDo(document("get-access_token-client_credentials-grantType",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Basic [clientId:clientSecret](Base64 Encoding Value)")
                        ),
                        requestParameters(
                                parameterWithName("scope").description("access token scope").optional(),
                                parameterWithName("grant_type").description("access token grant type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CACHE_CONTROL).description("cache control"),
                                headerWithName(HttpHeaders.PRAGMA).description("pragma"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName("X-Content-Type-Options").description("X-Content-Type-Options"),
                                headerWithName("X-XSS-Protection").description("X-XSS-Protection"),
                                headerWithName("X-Frame-Options").description("X-Frame-Options")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("access token"),
                                fieldWithPath("token_type").description("access token type"),
                                fieldWithPath("expires_in").description("access token expires time"),
                                fieldWithPath("scope").description("access scopes")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰 Client Credentials 방식으로 HttpBasic 헤더 값 없어서 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_ClientCredentials_No_HttpBasic_401() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .param("grant_type", GrantType.CLIENT_CREDENTIALS.toString()))
                .andExpect(status().isUnauthorized())
                .andDo(print())
        ;
    }

    /**
     * 인증 서버 엑세스 토큰 Client Credentials 방식으로 부정확한 HttpBasic 헤더 값 입력으로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_ClientCredentials_Invalid_HttpBasic_401() throws Exception {
        getAccessTokenClientCredentialsGrantTypeResponse("invalid_client_id", "invalid_client_secret")
                .andExpect(status().isUnauthorized())
                .andDo(print())
        ;
    }

    /**
     * Client Credentials 방식 엑세스 토큰 발급 요청 공통 로직
     * @param clientId 클라이언트 아이디
     * @param clientSecret 클라이언트 시크릿
     * @return Client Credentials 방식 엑세스 토큰 발급 요청 결과
     * @throws Exception
     */
    private ResultActions getAccessTokenClientCredentialsGrantTypeResponse(String clientId, String clientSecret) throws Exception {
        return mockMvc.perform(post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))
                .param("grant_type", GrantType.CLIENT_CREDENTIALS.toString()));
    }

    /**
     * Refresh Token 방식 엑세스 토큰 발급 요청 공통 로직
     * @param refresh_token 재발급 토큰
     * @return Refresh Token 방식 엑세스 토큰 발급 요청 결과
     * @throws Exception
     */
    private ResultActions getAccessTokenRefreshTokenGrantTypeResponse(String refresh_token) throws Exception {
        return mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("refresh_token", refresh_token)
                .param("grant_type", GrantType.REFRESH_TOKEN.toString()));
    }

    /**
     * Password 방식 엑세스 토근 발급 요청 공통 로직
     * @param email 이메일
     * @param password 비밀번호
     * @return Password 방식 엑세스 토큰 발급 요청 결과
     * @throws Exception
     */
    private ResultActions getAccessTokenPasswordGrantTypeResponse(String email, String password) throws Exception {
        return mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("username", email)
                .param("password", password)
                .param("grant_type", GrantType.PASSWORD.toString()));
    }

}