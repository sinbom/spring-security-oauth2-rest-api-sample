package me.nuguri.auth.controller;

import me.nuguri.auth.common.BaseIntegrationTest;
import me.nuguri.auth.domain.LoginRequest;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.service.AccountService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AccountApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 유저 로그인 API 방식 리턴 JSON 타입, 성공적으로 로그인 하는 경우
     * @throws Exception
     */
    @Test
    public void login_V1_Success_200() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                .content(objectMapper.writeValueAsString(new LoginRequest(properties.getAdminEmail(), properties.getAdminPassword())))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("sessionId").exists())
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("login-api",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type"),
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("sessionId").description("session id")
                        )
                )
        );
    }

    /**
     * 유저 로그인 API 방식 리턴 JSON 타입, 잘못된 유저 정보로 로그인 실패하는 경우
     * @throws Exception
     */
    @Test
    public void login_V1_Invalid_400() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                .content(objectMapper.writeValueAsString(new LoginRequest("invalidUser", "invalidPw")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andDo(print());
    }

    /**
     * 유저 정보 리스트 성공적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Success_200() throws Exception {
        generateUser30();
        mockMvc.perform(get("/api/v1/users")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,email,desc"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("queryUsers",
                        links(
                                linkWithRel("first").description("first page link"),
                                linkWithRel("prev").description("prev page link"),
                                linkWithRel("self").description("self link"),
                                linkWithRel("next").description("next page link"),
                                linkWithRel("last").description("last page link"),
                                linkWithRel("document").description("document")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header")
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
                                fieldWithPath("_embedded.getUserResponseList[*].id").description("account id"),
                                fieldWithPath("_embedded.getUserResponseList[*].email").description("account email"),
                                fieldWithPath("_embedded.getUserResponseList[*].roles").description("account roles"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.self.href").description("self link"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.getUser.href").description("getUser link"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.updateUser.href").description("updateUser link"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.mergeUser.href").description("mergeUser link"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.deleteUser.href").description("deleteUser link"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.self.type").description("self link http method type"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.getUser.type").description("getUser link http method type"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.updateUser.type").description("updateUser link http method type"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.mergeUser.type").description("mergeUser link http method type"),
                                fieldWithPath("_embedded.getUserResponseList[*]._links.deleteUser.type").description("deleteUser link http method type"),
                                fieldWithPath("_links.first.href").description("first page link"),
                                fieldWithPath("_links.prev.href").description("prev page link"),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.next.href").description("next page link"),
                                fieldWithPath("_links.last.href").description("last page link"),
                                fieldWithPath("_links.document.href").description("document"),
                                fieldWithPath("page.size").description("page size"),
                                fieldWithPath("page.totalElements").description("total element count"),
                                fieldWithPath("page.totalPages").description("total page count"),
                                fieldWithPath("page.number").description("current page number")
                        )
                )
        );
    }

    /**
     * 유저 정보 리스트 로그인 하지 않아서 실패하는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_No_Authentication_3XX() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/login"));
    }

    /**
     * 유저 정보 리스트 잘못된 엑세스 토큰으로 못 얻는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Invalid_Params_400() throws Exception {
        String[] pages = {"1-0", "06", "-98"};
        String[] sizes = {"asd", "08", "--12"};
        String[] sorts = {"zxczxczxc,zxc", "zxczxczxc", "id,qwe"};

        for (int i = 0; i < pages.length; i++) {
            mockMvc.perform(get("/api/v1/users")
                    .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                    .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                    .queryParam("page", pages[i])
                    .queryParam("size", sizes[i])
                    .queryParam("sort", sorts[i]))
                    .andDo(print())
                    .andExpect(jsonPath("timestamp").exists())
                    .andExpect(jsonPath("status").exists())
                    .andExpect(jsonPath("error").exists())
                    .andExpect(jsonPath("message").exists())
                    .andExpect(status().isBadRequest());
        }

    }

    /**
     * 관리자가 유저 정보 성공적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getUser_V1_Admin_Success_200() throws Exception {
        setAdminAuthentication();
        Account account = accountService.generate(Account.builder()
                .email("test@test.com")
                .password("test")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());

        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("getUser",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("updateUser").description("next page link"),
                                linkWithRel("mergeUser").description("last page link"),
                                linkWithRel("deleteUser").description("last page link")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header")
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
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.document.href").description("document"),
                                fieldWithPath("_links.updateUser.href").description("update user link"),
                                fieldWithPath("_links.mergeUser.href").description("merge user link"),
                                fieldWithPath("_links.deleteUser.href").description("delete user link"),
                                fieldWithPath("_links.self.type").description("self link http method type"),
                                fieldWithPath("_links.updateUser.type").description("update user link  http method type"),
                                fieldWithPath("_links.mergeUser.type").description("merge user link  http method type"),
                                fieldWithPath("_links.deleteUser.type").description("delete user link  http method type"),
                                fieldWithPath("id").description("account id"),
                                fieldWithPath("email").description("account email"),
                                fieldWithPath("roles").description("account roles")
                        )
                )
        );
    }

    /**
     * 사용자 자신의 유저 정보 성공적으로 얻는 경우
     * @throws Exception
     */
    @Test
    public void getUser_V1_User_Success_200() throws Exception {
        setUserAuthentication();
        Optional<Account> optionalAccount = accountService.find(properties.getUserEmail());
        mockMvc.perform(get("/api/v1/user/{id}", optionalAccount.get().getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 사용자가 자신의 유저 정보가 아닌 다른 정보를 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getUser_V1_User_Forbidden_403() throws Exception {
        setUserAuthentication();
        Account account = accountService.generate(Account.builder()
                .email("bvcncvbncvnbt@test.com")
                .password("test")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());
        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isForbidden());
    }

    /**
     * 유저 정보 로그인 하지 않아서 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getUser_V1_No_Authentication_3XX() throws Exception {
        Account account = accountService.generate(Account.builder()
                .email("sadsadasd@test.com")
                .password("asdsadsad")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());

        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/login"));
    }

    /**
     * 유저 정보 잘못된 식별자로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getUser_V1_Invalid_400() throws Exception {
        mockMvc.perform(get("/api/v1/user/{id}", "asdasd")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * 유저 정보 존재하지 않는 식별자로 얻지 못하는 경우
     * @throws Exception
     */
    @Test
    public void getUser_No_Exist_404() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(get("/api/v1/user/{id}", "1928361836")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isNotFound());
    }

    @Test
    public void generateUser_V1_Success_200() throws Exception {

    }

    /**
     * 테스트 계정 30개 생성
     */
    private void generateUser30() {
        IntStream.range(0, 30).forEach(n ->
                accountService.generate(Account.builder()
                        .email(UUID.randomUUID().toString() + "@test.com")
                        .password("test")
                        .roles(new HashSet<>(Arrays.asList(Role.USER)))
                        .build())
        );
    }

}