package me.nuguri.auth.controller.api;

import me.nuguri.auth.common.BaseIntegrationTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientApiControllerTest extends BaseIntegrationTest {

    @Test
    public void generateClient_V1_Success_200() throws Exception {
        setUserAuthentication();
        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "https://www.naver.com";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("nuguri", "test");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("redirectUri").value(redirectUri))
                .andExpect(jsonPath("resourceIds[0]").value(resourceIds.get(0)))
                .andExpect(jsonPath("resourceIds[1]").value(resourceIds.get(1)));
    }

}