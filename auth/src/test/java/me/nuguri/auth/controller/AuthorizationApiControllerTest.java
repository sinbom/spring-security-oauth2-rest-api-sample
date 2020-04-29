package me.nuguri.auth.controller;

import me.nuguri.auth.common.BaseIntegrationTest;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.enums.GrantType;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.enums.Scope;
import me.nuguri.auth.properties.AuthServerConfigProperties;
import me.nuguri.auth.repository.ClientRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AuthServerConfigProperties authServerConfigProperties;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 인증 서버 엑세스 토큰 정상적으로 만료되는 경우
     * @throws Exception
     */
    @Test
    public void revokeAccessToken_Success_200() throws Exception {
        String access_token = (String) new JacksonJsonParser()
                .parseMap(mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                        .param("username", authServerConfigProperties.getAdminEmail())
                        .param("password", authServerConfigProperties.getAdminPassword())
                        .param("grant_type", GrantType.PASSWORD.toString()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()).get("access_token");

        mockMvc.perform(post("/oauth/revoke_token")
                .header(HttpHeaders.AUTHORIZATION, access_token))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("revoke-access_token",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer [access token]")
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
                                fieldWithPath("scope").description("access scopes"),
                                fieldWithPath("id").description("account id")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰  전달하지 않아서 만료하지 못하는 경우
     * @throws Exception
     */
    @Test
    public void revokeAccessToken_No_AccessToken_401() throws Exception {
        mockMvc.perform(post("/oauth/revoke_token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 인증 서버 엑세스 토큰 부정확하거나 존재하지 않는 엑세스 토큰 입력으로 만료하지 못하는 경우
     * @throws Exception
     */
    @Test
    public void revokeAccessToken_Invalid_AccessToken_400() throws Exception {
        mockMvc.perform(post("/oauth/revoke_token")
                .header(HttpHeaders.AUTHORIZATION, "invalid_token_!(@*#&!"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * 인증 서버 엑세스 토큰을 전달해 현재 토큰의 유저 정보를 성공적으로 조회하는 경우
     * @throws Exception
     */
    @Test
    public void getMe_Success_200() throws Exception {
        String access_token = (String) new JacksonJsonParser()
                .parseMap(mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(authServerConfigProperties.getClientId(), authServerConfigProperties.getClientSecret()))
                        .param("username", authServerConfigProperties.getAdminEmail())
                        .param("password", authServerConfigProperties.getAdminPassword())
                        .param("grant_type", GrantType.PASSWORD.toString()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()).get("access_token");

        mockMvc.perform(get("/oauth/me")
                .header(HttpHeaders.AUTHORIZATION, access_token))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("get-me",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer [access token]")
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
                                fieldWithPath("email").description("account email"),
                                fieldWithPath("roles").description("account roles")
                        )
                ));
    }

    /**
     * 인증 서버 엑세스 토큰을 전달하지 않아 유저 정보를 조회하지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getMe_No_AccessToken_401() throws Exception {
        mockMvc.perform(get("/oauth/me"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    /**
     * 잘못된 인증 서버 엑세스 토큰으 전달해서 유저 정보를 조회하지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getMe_Invalid_AccessToken_400() throws Exception {
        mockMvc.perform(get("/oauth/me")
                .header(HttpHeaders.AUTHORIZATION, "invalid_token_!(@*#&!"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

/*    @Test
    public void registerClient_Success_200() throws Exception {
        String client_id = "test";
        String client_secret = "testing";
        clientRepository.save(Client.builder()
                .client_id(client_id)
                .client_secret(passwordEncoder.encode(client_secret))
                .scope(Scope.READ.toString())
                .authorities(Role.USER.toString())
                .web_server_redirect_uri("https://www.naver.com")
                .authorized_grant_types(GrantType.CLIENT_CREDENTIALS.toString())
                .resource_ids("test")
                .autoapprove(null)
                .build());

        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(client_id, client_secret))
                .param("grant_type", GrantType.CLIENT_CREDENTIALS.toString()))
                .andExpect(status().isOk());
    }*/

}