package me.nuguri.auth.controller;

import me.nuguri.auth.common.BaseIntegrationTest;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.properties.AuthServerConfigProperties;
import me.nuguri.auth.service.AccountService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthServerConfigProperties authServerConfigProperties;

    /**
     * 유저 정보 성공적으로 얻는 경우, 관리자 권한 엑세스 토큰
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Success_200() throws Exception {
        generateUser30();
        mockMvc.perform(get("/api/v1/users")
                .with(user(authServerConfigProperties.getAdminEmail()).password(authServerConfigProperties.getAdminPassword()))
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
     * 유저 정보 로그인 하지 않아서 실패하는 경우
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
     * 유저 정보 잘못된 엑세스 토큰으로 못 얻는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Invalid_Params_400() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .with(user(authServerConfigProperties.getAdminEmail()).password(authServerConfigProperties.getAdminPassword()))
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1-0")
                .queryParam("size", "-12")
                .queryParam("sort", "zxczxczxc,zxc"))
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