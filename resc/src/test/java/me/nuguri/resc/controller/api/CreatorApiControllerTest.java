package me.nuguri.resc.controller.api;

import me.nuguri.common.entity.Account;
import me.nuguri.resc.common.BaseIntegrationTest;
import me.nuguri.resc.entity.Book;
import me.nuguri.resc.entity.Company;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.repository.CompanyRepository;
import me.nuguri.resc.service.CreatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("저자 API 테스트")
public class CreatorApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private CreatorService creatorService;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * 테스트 메소드 실행 전, 저자와 책 엔티티 연관관계를 설정하고 영속화
     */
    @BeforeEach
    public void beforeEach() {
        List<Creator> creatorList = new ArrayList<>();
        String[] creatorNames = {"홍길동", "아무개", "김똥개", "신나라", "박대기"};
        LocalDate[] creatorBirth = {LocalDate.of(1946, 9, 17), LocalDate.of(1926, 5, 21)
                , LocalDate.of(1996, 10, 25), LocalDate.of(1920, 6, 13), LocalDate.of(1965, 4, 2)};
        LocalDate[] creatorDeath = {LocalDate.of(2018, 12, 1), LocalDate.of(2006, 4, 19)
                , LocalDate.of(2020, 2, 14), LocalDate.of(1987, 7, 11), LocalDate.of(1999, 9, 12)};

        for (int i = 0; i < creatorNames.length; i++) {
            Creator creator = new Creator();
            creator.setName(creatorNames[i]);
            creator.setBirth(creatorBirth[i]);
            creator.setDeath(creatorDeath[i]);
            creator.setGender(i % 2 == 0 ? Gender.M : Gender.F);
            creatorList.add(creator);

            creatorService.generate(creator);
        }

        long min = LocalDate.of(1900, 1, 1).toEpochDay();
        long max = LocalDate.now().toEpochDay();
        for (int i = 0; i < 200; i++) {
            Book book = new Book();
            book.setName("Test Book " + i);
            book.setPublishDate(LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(min, max)));
            book.addCreator(creatorList.get(new Random().nextInt(creatorList.size())));
            Company company = new Company();
            company.setName("아무회사" + i);
            company.setEstablishDate(LocalDate.now());
            book.addCompany(company);

            companyRepository.save(company);
        }

        entityManager.clear(); // 테스트 시 조회 쿼리 정확히 보기 위해서 영속성 콘텍스트 초기화
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 성공적인 경우")
    public void queryCreators_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);

        mockMvc.perform(get("/api/v1/creators")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @ParameterizedTest(name = "{index}. {displayName} parameter(sort: {arguments})")
    @DisplayName("저자 목록 페이징 조건 값 조회 성공적인 경우")
    @ValueSource(strings = {"id,asc", "id,name,desc", "id,name,birth,asc", "death", ""})
    public void conditionQueryCreators_V1_Success_200(String sort) throws Exception {
        mockRestTemplate(HttpStatus.OK);

        mockMvc.perform(get("/api/v1/creators")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("gender", Gender.M.toString())
                .queryParam("startBirth", "1990-01-01")
                .queryParam("endBirth", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()))
                .queryParam("startDeath", "1990-01-01")
                .queryParam("endDeath", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()))
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", sort))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 목록 페이징 조회 잘못된 엑세스 토큰으로 실패하는경우")
    public void queryCreators_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        mockMvc.perform(get("/api/v1/creators")
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
    public void queryCreators_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/creators")
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
    public void queryCreators_V1_Notfound404() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/creators")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "123123"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 성공적인 경우")
    public void getCreator_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        Creator creator = generateCreator();
        mockMvc.perform(get("/api/v1/creator/{id}", creator.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 잘못된 엑세스 토큰을 실패하는 경우")
    public void getCreator_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        Creator creator = generateCreator();
        mockMvc.perform(get("/api/v1/creator/{id}", creator.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "InvalidToken")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 잘못된 입력 값으로 실패하는 경우")
    public void getCreator_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/creator/{id}", "asdsda")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 조회 존재하지 않아서 실패하는 경우")
    public void getCreator_V1_NotFound_404() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(get("/api/v1/creator/{id}", 123123)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 성공적인 경우")
    public void generateCreator_V1_Success_201() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        request.setName("Test Creator");
        request.setBirth(LocalDate.of(1996, 9, 17));
        request.setDeath(LocalDate.now());
        request.setGender(Gender.M);

        mockMvc.perform(post("/api/v1/creator")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 잘못된 엑세스 토큰으로 실패하는 경우")
    public void generateCreator_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        request.setName("Test Creator");
        request.setBirth(LocalDate.of(1996, 9, 17));
        request.setDeath(LocalDate.now());

        mockMvc.perform(post("/api/v1/creator")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 생성 잘못된 입력 값으로 실패하는 경우")
    public void generateCreator_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        request.setName("");
        request.setBirth(LocalDate.now());
        request.setDeath(LocalDate.of(1996, 9, 17));

        mockMvc.perform(post("/api/v1/creator")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 부분 수정 성공적인 경우")
    public void updateCreator_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();

        Creator creator = generateCreator();
        Long id = creator.getId();
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(patch("/api/v1/creator/{id}", id)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
        Creator updated = creatorService.find(id);

        assertEquals(creator.getName(), updated.getName());
        assertEquals(gender, updated.getGender());
        assertEquals(birth, updated.getBirth());
        assertEquals(death, updated.getDeath());
    }

    @Test
    @DisplayName("저자 부분 수정 잘못된 엑세스 토큰으로 실패하는 경우")
    public void updateCreator_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        String name = "TEST";
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setName(name);
        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(patch("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 부분 수정 잘못된 입력 값으로 실패하는 경우")
    public void updateCreator_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        LocalDate birth = LocalDate.now();
        LocalDate death = LocalDate.of(1996, 9, 17);

        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(patch("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 부분 수정 존재하지 않아서 실패하는 경우")
    public void updateCreator_V1_NotFound_404() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        String name = "TEST";
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setName(name);
        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(patch("/api/v1/creator/{id}", 123641564)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 전체 수정 성공적인 경우")
    public void mergeCreator_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        Long id = generateCreator().getId();
        String name = "TEST";
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setName(name);
        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(put("/api/v1/creator/{id}", id)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        Creator merged = creatorService.find(id);

        assertEquals(name, merged.getName());
        assertEquals(gender, merged.getGender());
        assertEquals(birth, merged.getBirth());
        assertEquals(death, merged.getDeath());
    }

    @Test
    @DisplayName("저자 전체 수정 잘못된 엑세스 토큰으로 실패하는 경우")
    public void mergeCreator_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        String name = "TEST";
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setName(name);
        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(put("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 전체 수정 존재하지 않아서 생성하는 경우")
    public void mergeCreator_V1_NotFound_201() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        String name = "TEST";
        Gender gender = Gender.F;
        LocalDate birth = LocalDate.of(1996, 9, 17);
        LocalDate death = LocalDate.now();

        request.setName(name);
        request.setGender(gender);
        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(put("/api/v1/creator/{id}", 123641564)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 전체 수정 잘못된 입력 값으로 실패하는 경우")
    public void mergeCreator_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        CreatorApiController.GenerateCreatorRequest request = new CreatorApiController.GenerateCreatorRequest();
        LocalDate birth = LocalDate.now();
        LocalDate death = LocalDate.of(1996, 9, 17);

        request.setBirth(birth);
        request.setDeath(death);

        mockMvc.perform(put("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 삭제 성공적인 경우")
    public void deleteCreator_V1_Success_200() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(delete("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 삭제 잘못된 엑세스 토큰으로 실패하는 경우")
    public void deleteCreator_V1_Unauthorized_401() throws Exception {
        mockRestTemplate(HttpStatus.UNAUTHORIZED);
        mockMvc.perform(delete("/api/v1/creator/{id}", generateCreator().getId())
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid token"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 삭제 잘못된 파라미터로 실패하는 경우")
    public void deleteCreator_V1_Invalid_400() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(delete("/api/v1/creator/{id}", "asdsad")
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("저자 삭제 존재하지 않아서 실패하는 경우")
    public void deleteCreator_V1_NotFound_404() throws Exception {
        mockRestTemplate(HttpStatus.OK);
        mockMvc.perform(delete("/api/v1/creator/{id}", "1298371")
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    /**
     * 5개의 책을 쓴 저자 엔티티를 생성하는 공통 메소드
     *
     * @return 저자 엔티티
     */
    private Creator generateCreator() {
        Creator creator = new Creator();
        creator.setName("Test Creator");
        creator.setBirth(LocalDate.of(1996, 9, 17));
        creator.setDeath(LocalDate.now());
        creator.setGender(Gender.M);

        creatorService.generate(creator);

        IntStream.range(1, 5).forEach(n -> {
            Book book = new Book();
            book.setName("Test Book" + n);
            book.setPublishDate(LocalDate.now());
            book.addCreator(creator);

            Company company = new Company();
            company.setName("아무회사" + n);
            company.setEstablishDate(LocalDate.now());
            book.addCompany(company);

            companyRepository.save(company);
        });

        entityManager.clear(); // 테스트 시 조회 쿼리 정확히 보기 위해서 영속성 콘텍스트 초기화

        return creator;
    }

}