package me.nuguri.auth.controller.api;

import me.nuguri.auth.common.BaseIntegrationTest;
import me.nuguri.auth.entity.Account;
import me.nuguri.common.enums.Role;
import me.nuguri.auth.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("계정 API 테스트")
public class AccountApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("유저 로그인 API 방식 리턴 JSON 타입, 성공적으로 로그인 하는 경우")
    public void login_V1_Success_200() throws Exception {
        AccountApiController.LoginRequest request = new AccountApiController.LoginRequest();
        request.setUsername(properties.getAdminEmail());
        request.setPassword(properties.getAdminPassword());
        mockMvc.perform(post("/api/v1/login")
                .content(objectMapper.writeValueAsString(request))
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

    @Test
    @DisplayName("유저 로그인 API 방식 리턴 JSON 타입, 잘못된 유저 정보로 로그인 실패하는 경우")
    public void login_V1_Unauthorized_401() throws Exception {
        AccountApiController.LoginRequest request = new AccountApiController.LoginRequest();
        request.setUsername(properties.getAdminEmail());
        request.setPassword("1892378");

        mockMvc.perform(post("/api/v1/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("유저 로그인 API 방식 리턴 JSON 타입, 유저 정보를 입력하지 않아 로그인 실패하는 경우")
    public void login_V1_Invalid_400() throws Exception {
        AccountApiController.LoginRequest request = new AccountApiController.LoginRequest();
        request.setUsername("invalidUser");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("유저 정보 리스트 성공적으로 얻는 경우")
    public void queryUsers_V1_Success_200() throws Exception {
        generateUser30();
        mockMvc.perform(get("/api/v1/users")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()).roles(Role.ADMIN.toString()))
                .accept(MediaTypes.HAL_JSON)
                .queryParam("size", "10")
                .queryParam("sort", "id,email,desc"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("query-users",
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
                                fieldWithPath("_contents[*].id").description("account id"),
                                fieldWithPath("_contents[*].email").description("account email"),
                                fieldWithPath("_contents[*].roles").description("account roles"),
                                fieldWithPath("_contents[*]._links.self.href").description("self link"),
                                fieldWithPath("_contents[*]._links.getUser.href").description("getUser link"),
                                fieldWithPath("_contents[*]._links.updateUser.href").description("updateUser link"),
                                fieldWithPath("_contents[*]._links.mergeUser.href").description("mergeUser link"),
                                fieldWithPath("_contents[*]._links.deleteUser.href").description("deleteUser link"),
                                fieldWithPath("_contents[*]._links.self.type").description("self link http method type"),
                                fieldWithPath("_contents[*]._links.getUser.type").description("getUser link http method type"),
                                fieldWithPath("_contents[*]._links.updateUser.type").description("updateUser link http method type"),
                                fieldWithPath("_contents[*]._links.mergeUser.type").description("mergeUser link http method type"),
                                fieldWithPath("_contents[*]._links.deleteUser.type").description("deleteUser link http method type"),
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

    @DisplayName("유저 정보 리스트 관리자 권한 없어서 못 얻는 경우")
    public void queryUsers_V1_Forbidden_403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()).roles(Role.USER.toString()))
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("유저 정보 리스트 요청 페이지 데이터 없어서 못 얻는 경우")
    public void queryUsers_V1_NotFound_404() throws Exception {
         mockMvc.perform(get("/api/v1/users")
                 .queryParam("page", "18723671")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()).roles(Role.ADMIN.toString()))
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("유저 정보 리스트 잘못된 엑세스 토큰으로 못 얻는 경우")
    public void queryUsers_V1_Invalid_Params_400() throws Exception {
        String[] pages = {"1-0", "0", "-98"};
        String[] sizes = {"asd", "08", "--12"};
        String[] sorts = {"zxczxczxc,zxc", "zxczxczxc", "id,qwe"};

        for (int i = 0; i < pages.length; i++) {
            mockMvc.perform(get("/api/v1/users")
                    .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()).roles(Role.ADMIN.toString()))
                    .accept(MediaTypes.HAL_JSON)
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

    @Test
    @DisplayName("관리자가 유저 정보 성공적으로 얻는 경우")
    public void getUser_V1_Admin_Success_200() throws Exception {
        setAdminAuthentication();
        Account account = accountService.generate(Account.builder()
                .email("test@test.com")
                .password("test")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());

        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-user",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("updateUser").description("update user link"),
                                linkWithRel("mergeUser").description("merge user link"),
                                linkWithRel("deleteUser").description("delete user link")
                        ),
                        pathParameters(
                                parameterWithName("id").description("identifier of account")
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

    @Test
    @DisplayName("사용자 자신의 유저 정보 성공적으로 얻는 경우")
    public void getUser_V1_User_Success_200() throws Exception {
        setUserAuthentication();
        Account account = accountService.find(properties.getUserEmail());
        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자가 자신의 유저 정보가 아닌 다른 정보를 얻지 못하는 경우")
    public void getUser_V1_User_Forbidden_403() throws Exception {
        setUserAuthentication();
        Account account = accountService.generate(Account.builder()
                .email("bvcncvbncvnbt@test.com")
                .password("test")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());
        mockMvc.perform(get("/api/v1/user/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 잘못된 식별자로 얻지 못하는 경우")
    public void getUser_V1_Invalid_400() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(get("/api/v1/user/{id}", "asdasd")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 존재하지 않는 식별자로 얻지 못하는 경우")
    public void getUser_No_Exist_404() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(get("/api/v1/user/{id}", "1928361836")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유저 정보 생성 성공적인 경우")
    public void generateUser_V1_Success_201() throws Exception {
        AccountApiController.GenerateUserRequest request =  new AccountApiController.GenerateUserRequest();
        request.setEmail("test200@naver.com");
        request.setPassword("123123");
        request.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(post("/api/v1/user")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("generate-user",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("getUser").description("get user link"),
                                linkWithRel("updateUser").description("update user link"),
                                linkWithRel("mergeUser").description("merge user link"),
                                linkWithRel("deleteUser").description("delete user link")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type"),
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("created resource link"),
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
                                fieldWithPath("_links.getUser.href").description("get user link"),
                                fieldWithPath("_links.updateUser.href").description("update user link"),
                                fieldWithPath("_links.mergeUser.href").description("merge user link"),
                                fieldWithPath("_links.deleteUser.href").description("delete user link"),
                                fieldWithPath("_links.self.type").description("self link http method type"),
                                fieldWithPath("_links.getUser.type").description("get user link  http method type"),
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

    @Test
    @DisplayName("유저 정보 생성 잘못된 입력 정보로 실패하는 경우")
    public void generateUser_V1_Invalid_400() throws Exception {
        AccountApiController.GenerateUserRequest request =  new AccountApiController.GenerateUserRequest();
        request.setEmail("isNotEmailType");
        request.setPassword("1234");

        mockMvc.perform(post("/api/v1/user")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 생성 이미 존재하는 입력 정보로 실패하는 경우")
    public void generateUser_V1_Already_Exist_400() throws Exception {
        String email = "already@test.com";

        AccountApiController.GenerateUserRequest request =  new AccountApiController.GenerateUserRequest();
        request.setEmail(email);
        request.setPassword("124331");
        request.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

        accountService.generate(Account.builder()
                .email(email)
                .password("124332")
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());

        mockMvc.perform(post("/api/v1/user")
                .with(user(properties.getAdminEmail()).password(properties.getAdminPassword()))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("error").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 부분 수정 성공적인 경우")
    public void updateUser_V1_Success_200() throws Exception {
        setAdminAuthentication();

        Account account = accountService.find(properties.getUserEmail());
        String password = "123123";
        Set<Role> roles = new HashSet<>(Arrays.asList(Role.ADMIN));

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword(password);
        request.setRoles(roles);

        mockMvc.perform(patch("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("roles").value(Role.ADMIN.toString()))
                .andDo(document("update-user",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("getUser").description("get user link"),
                                linkWithRel("mergeUser").description("merge user link"),
                                linkWithRel("deleteUser").description("delete user link")
                        ),
                        pathParameters(
                                parameterWithName("id").description("identifier of account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type"),
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
                                fieldWithPath("_links.getUser.href").description("get user link"),
                                fieldWithPath("_links.mergeUser.href").description("merge user link"),
                                fieldWithPath("_links.deleteUser.href").description("delete user link"),
                                fieldWithPath("_links.self.type").description("self link http method type"),
                                fieldWithPath("_links.getUser.type").description("get user link  http method type"),
                                fieldWithPath("_links.mergeUser.type").description("merge user link  http method type"),
                                fieldWithPath("_links.deleteUser.type").description("delete user link  http method type"),
                                fieldWithPath("id").description("account id"),
                                fieldWithPath("email").description("account email"),
                                fieldWithPath("roles").description("account roles")
                        )
                )
        );;

        Account updated = accountService.find(properties.getUserEmail());
        assertThat(passwordEncoder.matches(password, updated.getPassword())).isTrue();
        assertThat(roles).isEqualTo(updated.getRoles());
    }

    @Test
    @DisplayName("유저 정보 부분 수정 유저 정보 존재하지 않는 경우")
    public void updateUser_V1_NotFound_404() throws Exception {
        setAdminAuthentication();

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1123123");
        request.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN)));

        mockMvc.perform(patch("/api/v1/user/{id}", "198237981")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유저 정보 부분 수정 잘못된 입력 값으로 실패하는 경우")
    public void updateUser_V1_Invalid_400() throws Exception {
        setAdminAuthentication();

        Account account = accountService.find(properties.getUserEmail());

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1");

        mockMvc.perform(patch("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 부분 수정 권한 없어서 실패하는 경우")
    public void updateUser_V1_Forbidden_403() throws Exception {
        setUserAuthentication();

        Account account = accountService.find(properties.getAdminEmail());

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1123123");
        request.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(patch("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 전체 수정 성공하는 경우")
    public void mergeUser_V1_Success_200() throws Exception {
        setAdminAuthentication();

        Account account = accountService.find(properties.getUserEmail());
        String password = "1123123";
        Set<Role> roles = new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER));

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword(password);
        request.setRoles(roles);

        mockMvc.perform(put("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("merge-user",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("getUser").description("get user link"),
                                linkWithRel("updateUser").description("update user link"),
                                linkWithRel("deleteUser").description("delete user link")
                        ),
                        pathParameters(
                                parameterWithName("id").description("identifier of account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type"),
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
                                fieldWithPath("_links.getUser.href").description("get user link"),
                                fieldWithPath("_links.updateUser.href").description("update user link"),
                                fieldWithPath("_links.deleteUser.href").description("delete user link"),
                                fieldWithPath("_links.self.type").description("self link http method type"),
                                fieldWithPath("_links.getUser.type").description("get user link  http method type"),
                                fieldWithPath("_links.updateUser.type").description("update user link  http method type"),
                                fieldWithPath("_links.deleteUser.type").description("delete user link  http method type"),
                                fieldWithPath("id").description("account id"),
                                fieldWithPath("email").description("account email"),
                                fieldWithPath("roles").description("account roles")
                        )
                )
        );

        Account merge = accountService.find(properties.getUserEmail());
        assertThat(passwordEncoder.matches(password, merge.getPassword())).isTrue();
        assertThat(roles).isEqualTo(merge.getRoles());
    }

    @Test
    @DisplayName("유저 정보 전체 수정 권한 없어서 실패하는 경우")
    public void mergeUser_V1_Success_403() throws Exception {
        setUserAuthentication();

        Account account = accountService.find(properties.getAdminEmail());

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1123123");
        request.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(put("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 전체 수정 유저 정보 존재하지 않는 경우")
    public void mergeUser_V1_NotFound_404() throws Exception {
        setAdminAuthentication();

        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1123123");
        request.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN)));

        mockMvc.perform(put("/api/v1/user/{id}", "198237981")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유저 정보 전체 수정 잘못된 입력 값으로 실패하는 경우")
    public void mergeUser_V1_Invalid_400() throws Exception {
        setAdminAuthentication();

        Account account = accountService.find(properties.getUserEmail());
        AccountApiController.UpdateUserRequest request = new AccountApiController.UpdateUserRequest();
        request.setPassword("1");
        request.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN)));

        mockMvc.perform(put("/api/v1/user/{id}", account.getId())
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 삭제 성공적인 경우")
    public void deleteUser_V1_Success_200() throws Exception {
        setAdminAuthentication();
        Account account = accountService.find(properties.getUserEmail());

        mockMvc.perform(delete("/api/v1/user/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("delete-user",
                        links(
                                linkWithRel("self").description("self link"),
                                linkWithRel("document").description("document"),
                                linkWithRel("getUser").description("get user link"),
                                linkWithRel("updateUser").description("update user link"),
                                linkWithRel("mergeUser").description("merge user link")
                        ),
                        pathParameters(
                                parameterWithName("id").description("identifier of account")
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
                                fieldWithPath("_links.getUser.href").description("get user link"),
                                fieldWithPath("_links.updateUser.href").description("update user link"),
                                fieldWithPath("_links.mergeUser.href").description("merge user link"),
                                fieldWithPath("_links.self.type").description("self link http method type"),
                                fieldWithPath("_links.getUser.type").description("get user link  http method type"),
                                fieldWithPath("_links.updateUser.type").description("update user link  http method type"),
                                fieldWithPath("_links.mergeUser.type").description("merge user link  http method type"),
                                fieldWithPath("id").description("account id"),
                                fieldWithPath("email").description("account email"),
                                fieldWithPath("roles").description("account roles")
                        )
                        )
                );
    }

    @Test
    @DisplayName("유저 정보 삭제 권한 없어서 실패하는 경우")
    public void deleteUser_V1_Forbidden_403() throws Exception {
        setUserAuthentication();
        Account account = accountService.find(properties.getAdminEmail());
        mockMvc.perform(delete("/api/v1/user/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 존재 하지 않아서 실패하는 경우")
    public void deleteUser_V1_Forbidden_404() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(delete("/api/v1/user/{id}", "123123123123")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유저 정보 잘못된 입력 값으로 실패하는 경우")
    public void deleteUser_V1_Invalid_400() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(delete("/api/v1/user/{id}", "asdasd")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
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