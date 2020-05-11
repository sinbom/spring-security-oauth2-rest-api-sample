package me.nuguri.auth.controller.api;

import me.nuguri.auth.common.BaseIntegrationTest;
import me.nuguri.common.enums.GrantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;

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

@DisplayName("Oauth2 API 테스트")
public class AuthorizationApiControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("인증 서버 엑세스 토큰 정상적으로 만료되는 경우")
    public void revokeAccessToken_Success_200() throws Exception {
        String access_token = (String) new JacksonJsonParser()
                .parseMap(mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(properties.getClientId(), properties.getClientSecret()))
                        .param("username", properties.getAdminEmail())
                        .param("password", properties.getAdminPassword())
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

    @Test
    @DisplayName("인증 서버 엑세스 토큰  전달하지 않아서 만료하지 못하는 경우")
    public void revokeAccessToken_No_AccessToken_401() throws Exception {
        mockMvc.perform(post("/oauth/revoke_token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("인증 서버 엑세스 토큰 부정확하거나 존재하지 않는 엑세스 토큰 입력으로 만료하지 못하는 경우")
    public void revokeAccessToken_Invalid_AccessToken_400() throws Exception {
        mockMvc.perform(post("/oauth/revoke_token")
                .header(HttpHeaders.AUTHORIZATION, "invalid_token_!(@*#&!"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("인증 서버 엑세스 토큰을 전달해 현재 토큰의 유저 정보를 성공적으로 조회하는 경우")
    public void getMe_Success_200() throws Exception {
        String access_token = (String) new JacksonJsonParser()
                .parseMap(mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(properties.getClientId(), properties.getClientSecret()))
                        .param("username", properties.getAdminEmail())
                        .param("password", properties.getAdminPassword())
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

    @Test
    @DisplayName("인증 서버 엑세스 토큰을 전달하지 않아 유저 정보를 조회하지 못하는 경우")
    public void getMe_No_AccessToken_401() throws Exception {
        mockMvc.perform(get("/oauth/me"))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("잘못된 인증 서버 엑세스 토큰으 전달해서 유저 정보를 조회하지 못하는 경우")
    public void getMe_Invalid_AccessToken_400() throws Exception {
        mockMvc.perform(get("/oauth/me")
                .header(HttpHeaders.AUTHORIZATION, "invalid_token_!(@*#&!"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists());
    }


}