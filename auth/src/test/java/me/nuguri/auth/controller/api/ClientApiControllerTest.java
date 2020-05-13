package me.nuguri.auth.controller.api;

import me.nuguri.auth.common.BaseIntegrationTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("클라이언트 API 테스트")
public class ClientApiControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("클라이언트 생성 성공적인 경우")
    public void generateClient_V1_Success_200() throws Exception {
        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "https://www.naver.com";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("nuguri", "test");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
                .with(httpBasic(properties.getUserEmail(), properties.getUserPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("redirectUri").value(redirectUri))
                .andExpect(jsonPath("resourceIds[0]").value(resourceIds.get(0)))
                .andExpect(jsonPath("resourceIds[1]").value(resourceIds.get(1)));
    }

}