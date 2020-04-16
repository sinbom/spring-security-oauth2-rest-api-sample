package me.nuguri.resource.controller;

import me.nuguri.resource.common.BaseControllerTest;
import me.nuguri.resource.common.ResourceServerConfigProperties;
import me.nuguri.resource.common.Role;
import me.nuguri.resource.common.TestProperties;
import me.nuguri.resource.entity.Account;
import me.nuguri.resource.repository.AccountRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResourceServerConfigProperties resourceServerConfigProperties;

    @Autowired
    private TestProperties testProperties;

    @After
    public void initAccount() {
        accountRepository.deleteAll();
    }

    /**
     * 유저 정보 성공적으로 얻는 경우, 관리자 권한 엑세스 토큰
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Success_200() throws Exception {
        String access_token = getAccessToken(testProperties.getAdminEmail(), testProperties.getUserPassword());
        generateUser30();
        mockMvc.perform(get("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access_token)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,desc"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer [access token]"),
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
     * 유저 정보 엑세스 토큰 없어서 못 얻는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_No_AccessToken_401() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 유저 정보 잘못된 엑세스 토큰으로 못 얻는 경우
     * @throws Exception
     */
    @Test
    public void queryUsers_V1_Invalid_AccessToken_401() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken!@*&#(*")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 테스트 게정 30개 생성
     */
    private void generateUser30() {
        IntStream.range(0, 30).forEach(n ->
                accountRepository.save(Account.builder()
                        .email("testId" + n + "@test.com")
                        .password("test" + n)
                        .roles(new HashSet<>(Arrays.asList(Role.USER)))
                        .build())
        );
    }

}