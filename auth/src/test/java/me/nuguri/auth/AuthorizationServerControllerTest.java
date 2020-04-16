package me.nuguri.auth;

import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.common.GrantType;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
public class AuthorizationServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthServerConfigProperties authServerConfigProperties;

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
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, access_token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("expiration").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.document.href").exists())
                .andDo(print())
                .andDo(document("revoke-access_token",
                        links(
                                linkWithRel("self").description("link self"),
                                linkWithRel("document").description("link document")
                        ),
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
                                fieldWithPath("expiration").description("access token expiration"),
                                fieldWithPath("scope").description("access scopes"),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.document.href").description("document link")
                        )
                )
        );
    }

    /**
     * 인증 서버 엑세스 토큰 부정확하거나 존재하지 않는 엑세스 토큰 입력으로 만료하지 못하는 경우
     * @throws Exception
     */
    @Test
    public void revokeAccessToken_Invalid_AccessToken_400() throws Exception {
        mockMvc.perform(post("/oauth/revoke_token")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, "invalid_token_!(@*#&!"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}