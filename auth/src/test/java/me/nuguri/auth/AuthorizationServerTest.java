package me.nuguri.auth;

import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.config.RestDocsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
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
     * 인증 서버 엑세스 토큰 정상적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_success_200() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                .param("username", authServerConfigProperties.getAdminEmail())
                .param("password", authServerConfigProperties.getAdminPassword())
                .param("grant_type", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andDo(print())
                .andDo(document("get-access_token",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Basic [clientId:clientSecret](Base64 Encoding Value)")
                        ),
                        requestParameters(
                                parameterWithName("username").description("user account email"),
                                parameterWithName("password").description("user account password"),
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
     * 인증 서버 엑세스 토큰 HttpBasic 헤더 값 없어서 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_No_HttpBasic_401() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .param("username", authServerConfigProperties.getAdminEmail())
                .param("password", authServerConfigProperties.getAdminPassword())
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 인증 서버 엑세스 토큰 부정확한 username 및 password 입력 으로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getAccessToken_Invalid_Username_401() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .param("username", "noexistemail@test.com")
                .param("password", "12341234")
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}