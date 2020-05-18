package me.nuguri.resc.controller.api;

import me.nuguri.resc.common.BaseIntegrationTest;
import me.nuguri.resc.entity.Author;
import me.nuguri.resc.entity.Book;
import me.nuguri.resc.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("저자 API 테스트")
public class AuthorApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    /**
     * 테스트 메소드 실행 전, 저자와 책 엔티티 연관관계를 설정하고 영속화
     */
    @BeforeEach
    public void beforeEach() {
        List<Author> authorList = new ArrayList<>();
        String[] authorNames = {"홍길동", "아무개", "김똥개", "신나라", "박대기"};
        LocalDate[] authorBirth = {LocalDate.of(1946, 9, 17), LocalDate.of(1926, 5, 21)
                , LocalDate.of(1996, 10, 25), LocalDate.of(1920, 6, 13), LocalDate.of(1965, 4,2)};
        LocalDate[] authorDeath = {LocalDate.of(2018, 12, 1), LocalDate.of(2006, 4, 19)
                , LocalDate.of(2020, 2, 14), LocalDate.of(1987, 7, 11), LocalDate.of(1999, 9, 12)};

        for (int i = 0; i < authorNames.length; i++) {
            Author author = new Author();
            author.setName(authorNames[i]);
            author.setBirth(authorBirth[i]);
            author.setDeath(authorDeath[i]);
            authorList.add(author);
        }

        long min = LocalDate.of(1900, 1, 1).toEpochDay();
        long max = LocalDate.now().toEpochDay();
        for (int i = 0; i < 200; i++) {
            Book book = new Book();
            book.setName("Test Book " + i);
            book.setPubDate(LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(min, max)));
            book.addAuthor(authorList.get(new Random().nextInt(authorList.size())));
        }
        authorRepository.saveAll(authorList);
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 성공적인 경우")
    public void queryAuthors_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);
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
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid")
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @ParameterizedTest(name = "{index}. {displayName} parameter(page: {0} / size: {1} / sort : {2}")
    @DisplayName("저자 목록 페이징 조회 잘못된 파라미터로 실패하는 경우")
    @CsvSource(value = {"1-0:0:-98", "asd:08:-12", "zxczxczxc,zxc:zxczxczxc:id,qwe"}, delimiter = ':')
    public void queryAuthors_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
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
        mockRestTemplate(HttpStatus.OK);
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
        mockRestTemplate(HttpStatus.OK);
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
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
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
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/author/{id}", "asdsda")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 존재하지 않아서 실패하는 경우")
    public void getAuthor_V1_NotFound_404() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/author/{id}", 123123)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 성공적인 경우")
    public void generateAuthor_V1_Success_201() throws Exception {
        mockRestTemplate(HttpStatus.OK);
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
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
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
        mockRestTemplate(HttpStatus.OK);
        AuthorApiController.GenerateAuthorRequest request = new AuthorApiController.GenerateAuthorRequest();
        request.setName("");
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
        Author author = new Author();
        author.setName("Test Author");
        author.setBirth(LocalDate.of(1996, 9, 17));
        author.setDeath(LocalDate.now());

        IntStream.range(1, 5).forEach(n -> {
            Book book = new Book();
            book.setName("Test Book" + n);
            book.setPubDate(LocalDate.now());
            book.addAuthor(author);
        });

        return authorRepository.save(author);
    }

}