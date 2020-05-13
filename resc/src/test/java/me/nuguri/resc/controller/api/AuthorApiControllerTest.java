package me.nuguri.resc.controller.api;

import me.nuguri.resc.common.BaseIntegrationTest;
import me.nuguri.resc.entity.Author;
import me.nuguri.resc.entity.Book;
import me.nuguri.resc.service.AuthorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("저자 API 테스트")
public class AuthorApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Test
    @DisplayName("저자 목록 페이징 조회 성공적인 경우")
    public void queryAuthors_V1_Success_200() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 잘못된 엑세스 토큰으로 실패하는경우")
    public void queryAuthors_V1_Unauthorized_401() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid")
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 잘못된 파라미터로 실패하는 경우")
    public void queryAuthors_V1_Invalid_400() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "01")
                .queryParam("size", "asd")
                .queryParam("sort", "zxc,nbv"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 컨텐츠 없는 페이지 요청으로 실패하는 경우")
    public void queryAuthors_V1_Notfound404() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "123123"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 성공적인 경우")
    public void getAuthor_V1_Success_200() throws Exception {
        Author author = generateAuthor();
        mockMvc.perform(get("/api/v1/author/{id}", author.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 잘못된 엑세스 토큰을 실패하는 경우")
    public void getAuthor_V1_Unauthorized_401() throws Exception {
        Author author = generateAuthor();
        mockMvc.perform(get("/api/v1/author/{id}", author.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "InvalidToken")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 잘못된 입력 값으로 실패하는 경우")
    public void getAuthor_V1_Invalid_400() throws Exception {
        mockMvc.perform(get("/api/v1/author/{id}", "asdsda")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 존재하지 않아서 실패하는 경우")
    public void getAuthor_V1_Notfound_404() throws Exception {
        mockMvc.perform(get("/api/v1/author/{id}", 123123)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 성공적인 경우")
    public void generateAuthor_V1_Success_201() throws Exception {
        AuthorApiController.GenerateAuthorRequest request = new AuthorApiController.GenerateAuthorRequest();
        request.setName("Test Author");
        request.setBirth(LocalDate.of(1996, 9, 17));
        request.setDeath(LocalDate.now());

        mockMvc.perform(post("/api/v1/author")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 잘못된 엑세스 토큰으로 실패하는 경우")
    public void generateAuthor_V1_Unauthorized_401() throws Exception {
        AuthorApiController.GenerateAuthorRequest request = new AuthorApiController.GenerateAuthorRequest();
        request.setName("Test Author");
        request.setBirth(LocalDate.of(1996, 9, 17));
        request.setDeath(LocalDate.now());

        mockMvc.perform(post("/api/v1/author")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 잘못된 입력 값으로 실패하는 경우")
    public void generateAuthor_V1_Invalid_400() throws Exception {
        AuthorApiController.GenerateAuthorRequest request = new AuthorApiController.GenerateAuthorRequest();
        request.setName("Test Author");
        request.setBirth(LocalDate.now());
        request.setDeath(LocalDate.of(1996, 9, 17));

        mockMvc.perform(post("/api/v1/author")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    /**
     * 5개의 책을 쓴 저자 엔티티를 생성하는 공통 메소드
     * @return 저자 엔티티
     */
    private Author generateAuthor() {
        Author author = Author.builder()
                .name("Test Author")
                .birth(LocalDate.of(1996, 9, 17))
                .death(LocalDate.now())
                .books(new ArrayList<>())
                .build();

        IntStream.range(1, 5).forEach(n -> {
            Book.builder()
                .name("Test Book" + n)
                .pubDate(LocalDate.now())
                .build()
                .addAuthor(author);
        });

        return authorService.generate(author);
    }



}