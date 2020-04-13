package me.nuguri.auth;

import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.common.GrantType;
import me.nuguri.auth.common.Scope;
import me.nuguri.auth.config.RestDocsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class AuthorizationServerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthServerConfigProperties authServerConfigProperties;


    /**
     * 인증 서버 엑세스 토큰 Password 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Password_success_200() throws Exception {
        getAccessTokenResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andDo(print())
                .andDo(document("get-access_token",
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
                .andExpect(status().isUnauthorized());
    }

    /**
     * 인증 서버 엑세스 토큰 Password 방식으로 부정확한 username 및 password 입력 으로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_Password_Invalid_Username_400() throws Exception {
        getAccessTokenResponse("noexistemail@test.com", "12341234")
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * 인증 서버 엑세스 토큰 Refresh Token 방식으로 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_GrantType_RefreshToken_success_200() throws Exception {
        String refresh_token = (String) new JacksonJsonParser()
                .parseMap(getAccessTokenResponse(authServerConfigProperties.getAdminEmail(), authServerConfigProperties.getAdminPassword())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()).get("refresh_token");

        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("refresh_token", refresh_token)
                .param("grant_type", GrantType.REFRESH_TOKEN.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }

    /**
     * Password 방식 엑세스 토근 발급 요청 공통 로직
     * @param email 이메일
     * @param password 비밀번호
     * @return Password 방식 엑세스 토큰 발급 요청 결과
     * @throws Exception
     */
    private ResultActions getAccessTokenResponse(String email, String password) throws Exception {
        return mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("username", email)
                .param("password", password)
                .param("grant_type", GrantType.PASSWORD.toString()));
    }

}