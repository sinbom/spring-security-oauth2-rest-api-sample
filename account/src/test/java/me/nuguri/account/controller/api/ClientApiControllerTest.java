package me.nuguri.account.controller.api;

import me.nuguri.account.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("클라이언트 API 테스트")
public class ClientApiControllerTest extends BaseIntegrationTest {


    @Test
    @DisplayName("클라이언트 생성 성공적인 경우")
    public void generateClient_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "https://www.naver.com";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("nuguri", "test");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("redirectUri").value(redirectUri))
                .andExpect(jsonPath("resourceIds[0]").value(resourceIds.get(0)))
                .andExpect(jsonPath("resourceIds[1]").value(resourceIds.get(1)));
    }

    @Test
    @DisplayName("클라이언트 생성 유효하지 않은 토큰으로 실패하는 경우")
    public void generateClient_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED, null);
        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "https://www.naver.com";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("nuguri", "test");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
                .header(HttpHeaders.AUTHORIZATION, "Bearer Invalid Token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

/*    @Test
    @DisplayName("클라이언트 생성 권한 없어서 실패하는 경우")
    public void generateClient_V1_Forbidden_403() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));

        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "https://www.naver.com";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("nuguri", "test");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }*/

    @Test
    @DisplayName("클라이언트 생성 잘못된 입력 값으로 실패하는 경우")
    public void generateClient_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getAdminEmail()));
        ClientApiController.GenerateClientRequest request = new ClientApiController.GenerateClientRequest();
        String redirectUri = "asdsadasd";
        request.setRedirectUri(redirectUri);
        List<String> resourceIds = Arrays.asList("unknown", "unknown2");
        request.setResourceIds(resourceIds);

        mockMvc.perform(post("/api/v1/client")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}