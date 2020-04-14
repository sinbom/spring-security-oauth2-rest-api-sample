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

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

        mockMvc.perform(get("/oauth/revoke-token")
                .header(HttpHeaders.AUTHORIZATION, access_token))
                .andDo(print())
                .andExpect(status().isOk());
    }


}