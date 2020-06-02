package me.nuguri.account.controller.api;

import me.nuguri.account.common.BaseIntegrationTest;
import me.nuguri.account.service.ClientService;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("클라이언트 API 테스트")
public class ClientApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Test
    @DisplayName("클라이언트 정보 리스트 성공적으로 얻는 경우")
    public void queryClients_V1_Success_200() throws Exception {
        generateClients();
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getAdminEmail()));
        mockMvc.perform(get("/api/v1/clients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 리스트 유효하지 않은 토큰으로 못 얻는 경우")
    public void queryClients_V1_Unauthorized_401() throws Exception {
        generateClients();
        mockRestTemplate(HttpStatus.UNAUTHORIZED, null);
        mockMvc.perform(get("/api/v1/clients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer Invalid Token")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 리스트 권한 없어서 못 얻는 경우")
    public void queryClients_V1_Forbidden_403() throws Exception {
        generateClients();
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        mockMvc.perform(get("/api/v1/clients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @ParameterizedTest(name = "{index}. {displayName} parameter(page: {0} / size: {1} / sort : {2}")
    @DisplayName("클라이언트 정보 리스트 잘못된 파라미터로 못 얻는 경우")
    @CsvSource(value = {"1-0:0:-98", "asd:08:-12", "0:-2:id,qwe"}, delimiter = ':')
    public void queryClients_V1_Invalid_400(String page, String size, String sort) throws Exception {
        generateClients();
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        mockMvc.perform(get("/api/v1/clients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 리스트 요청 페이지 데이터 없어서 못 얻는 경우")
    public void queryClients_V1_NotFound_404() throws Exception {
        generateClients();
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getAdminEmail()));
        mockMvc.perform(get("/api/v1/clients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1237651"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 성공적으로 얻는 경우")
    public void getClient_V1_Admin_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getAdminEmail()));
        mockMvc.perform(get("/api/v1/client{id}", clientService.find(properties.getClientId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 유효하지 않은 토큰으로 못 얻는 경우")
    public void getClient_V1_Admin_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED, null);
        mockMvc.perform(get("/api/v1/client{id}", clientService.find(properties.getClientId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer Invalid Token")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 권한 없어서 얻지 못 얻는 경우")
    public void getClient_V1_Admin_Forbidden_403() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        mockMvc.perform(get("/api/v1/client{id}", clientService.find(properties.getClientId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 잘못된 식별자로 못 얻는 경우")
    public void getClient_V1_Admin_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        mockMvc.perform(get("/api/v1/client{id}", "asdasa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("클라이언트 정보 존재하지 않는 식별자로 못 얻는 경우")
    public void getClient_V1_Admin_NotFound_404() throws Exception {
        mockRestTemplate(HttpStatus.OK, accountService.find(properties.getUserEmail()));
        mockMvc.perform(get("/api/v1/client{id}", "12312312")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

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

    private void generateClients() {
        Account account = accountService.find(properties.getAdminEmail());
        IntStream.range(0, 30).forEach(n -> {
            Client client = new Client();
            client.setClientId(UUID.randomUUID().toString());
            client.setClientSecret(UUID.randomUUID().toString());
            client.setGrantTypes(GrantType.AUTHORIZATION_CODE.toString());
            client.setAuthorities(account.getRole().toString());
            client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
            client.setRedirectUri("https://www.test.com");
            client.setResourceIds(String.join(",", "account", "nuguri"));
            client.addAccount(account);
            clientService.generate(client);
        });
    }

}