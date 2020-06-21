package me.nuguri.common.support;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.common.entity.*;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.GrantType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 개발 및 테스트시 초기 엔티티 설정용 임시 이니셜라이저
 */
@Slf4j
@RequiredArgsConstructor
public class EntityInitializer {

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void init(EntityManager em) {
        log.info("[log] EntityInitializer init entities");
        // 리소스 서버 식별 엔티티 생성
        List<Resource> resources = new ArrayList<>();
        Arrays.asList("account", "nuguri", "test")
                .forEach(s -> {
                    Resource resource = Resource.builder()
                            .name(s)
                            .build();
                    em.persist(resource);
                    resources.add(resource);
                });
        // 접근 범위 엔티티 생성
        List<Scope> scopes = new ArrayList<>();
        Arrays.asList("read", "write")
                .forEach(s -> {
                    Scope scope = Scope.builder()
                            .name(s)
                            .build();
                    em.persist(scope);
                    scopes.add(scope);
                });
        // 접근 권한 엔티티 생성
        List<Authority> authorities = new ArrayList<>();
        Arrays.asList("ADMIN", "USER")
                .forEach(s -> {
                    Authority authority = Authority.builder()
                            .name(s)
                            .build();
                    em.persist(authority);
                    authorities.add(authority);
                });
        // 사용자 엔티티 생성
        Account admin = Account.builder()
                .name("관리자")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("1234"))
                .gender(Gender.M)
                .address(new Address("경기도 과천시", "부림2길 76 2층", "13830"))
                .authority(authorities.get(0))
                .build();
        Account user = Account.builder()
                .name("사용자")
                .email("user@naver.com")
                .password(passwordEncoder.encode("1234"))
                .gender(Gender.F)
                .address(new Address("경기도 안양시", "한미아파트 502호", "12314"))
                .authority(authorities.get(1))
                .build();
        em.persist(admin);
        em.persist(user);
        // 클라이언트 엔티티 생성
        Client adminClient = Client.builder()
                .clientId("nuguri")
                .clientSecret(passwordEncoder.encode("bom"))
                .account(admin)
                .build();
        Client userClient = Client.builder()
                .clientId("test")
                .clientSecret(passwordEncoder.encode("test"))
                .account(user)
                .build();
        em.persist(adminClient);
        em.persist(userClient);
        // 클라이언트 접근 권한 매핑 엔티티 생성
        ClientAuthority adminClientAuthtorityMapping1 = ClientAuthority.builder()
                .client(adminClient)
                .authority(authorities.get(0))
                .build();
        ClientAuthority adminClientAuthorityMapping2 = ClientAuthority.builder()
                .client(adminClient)
                .authority(authorities.get(1))
                .build();
        ClientAuthority userClientAuthorityMapping1 = ClientAuthority.builder()
                .client(userClient)
                .authority(authorities.get(1))
                .build();
        em.persist(adminClientAuthtorityMapping1);
        em.persist(adminClientAuthorityMapping2);
        em.persist(userClientAuthorityMapping1);
        // 클라이언트 접근 범위 매핑 엔티티 생성
        ClientScope adminClientScopeMapping1 = ClientScope.builder()
                .client(adminClient)
                .scope(scopes.get(0))
                .build();
        ClientScope adminClientScopeMapping2 = ClientScope.builder()
                .client(adminClient)
                .scope(scopes.get(1))
                .build();
        ClientScope userClientScopeMapping1 = ClientScope.builder()
                .client(userClient)
                .scope(scopes.get(0))
                .build();
        em.persist(adminClientScopeMapping1);
        em.persist(adminClientScopeMapping2);
        em.persist(userClientScopeMapping1);
        // 클라이언트 인증 부여 방식 매핑 엔티티 생성
        GrantType[] grantTypes = GrantType.values();
        Arrays.stream(grantTypes)
                .forEach(g ->
                        em.persist(ClientGrantType.builder()
                                .client(adminClient)
                                .grantType(g)
                                .build())
                );
        ClientGrantType userClientGrantTypeMapping1 = ClientGrantType.builder()
                .client(userClient)
                .grantType(GrantType.AUTHORIZATION_CODE)
                .build();
        ClientGrantType userClientGrantTypeMapping2 = ClientGrantType.builder()
                .client(userClient)
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .build();
        em.persist(userClientGrantTypeMapping1);
        em.persist(userClientGrantTypeMapping2);
        // 클라이언트 접근 리소스 매핑 엔티티 생성
        resources.forEach(r ->
                em.persist(ClientResource.builder()
                        .client(adminClient)
                        .resource(r)
                        .build())
        );
        resources.stream()
                .limit(2)
                .forEach(r ->
                        em.persist(ClientResource.builder()
                                .client(userClient)
                                .resource(r)
                                .build()
                        )
                );
        // 클라이언트 리다이렉트 매핑 엔티티 생성
        ClientRedirectUri adminClientRedirectMapping1 = ClientRedirectUri.builder()
                .uri("http://localhost:9600/main")
                .client(adminClient)
                .build();
        ClientRedirectUri userClientRedirectMapping1 = ClientRedirectUri.builder()
                .uri("http://localhost:9600/main")
                .client(userClient)
                .build();
        em.persist(adminClientRedirectMapping1);
        em.persist(userClientRedirectMapping1);
        // TODO 인증, 계정 서버 작업 정리후 정리할 것
        Random random = new Random();
        List<Creator> creators = new ArrayList<>();
        String[] authorNames = {"홍길동", "아무개", "김똥개", "신나라", "박대기"};
        LocalDate[] authorBirths = {
                LocalDate.of(1946, 9, 17),
                LocalDate.of(1926, 5, 21),
                LocalDate.of(1996, 10, 25),
                LocalDate.of(1920, 6, 13),
                LocalDate.of(1965, 4, 2)
        };
        LocalDate[] authorDeaths = {
                LocalDate.of(2018, 12, 1),
                LocalDate.of(2006, 4, 19),
                LocalDate.of(2020, 2, 14),
                LocalDate.of(1987, 7, 11),
                LocalDate.of(1999, 9, 12)
        };
        for (int i = 0; i < authorNames.length; i++) {
            Creator creator = Creator.builder()
                    .name(authorNames[i])
                    .birth(authorBirths[i])
                    .death(authorDeaths[i])
                    .gender(i % 2 == 0 ? Gender.M : Gender.F)
                    .build();
            creators.add(creator);
            em.persist(creator);
        }

        List<Book> books = new ArrayList<>();
        long min = LocalDate.of(1900, 1, 1).toEpochDay();
        long max = LocalDate.now().toEpochDay();
        for (int i = 0; i < 200; i++) {
            Company company = Company.builder()
                    .name("아무회사" + i)
                    .establishDate(LocalDate.now())
                    .build();

            Book book = Book.builder()
                    .name("책 " + i)
                    .publishDate(LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(min, max)))
                    .creator(creators.get(random.nextInt(creators.size())))
                    .company(company)
                    .build();

            books.add(book);
            em.persist(company);
        }

        List<Category> categories = new ArrayList<>();
        String[] majorCategoryNames = {"국내도서", "외국도서", "eBook"};

        for (String name : majorCategoryNames) {
            categories.add(Category.builder()
                    .name(name)
                    .build()
            );
        }

        List<List<Category>> subCategories = new ArrayList<>();
        String[][] subCategoryNames = {
                {"소설/시", "에세이", "인문", "역사", "예술", "종교", "사회", "과학", "경제/경영", "자기계발", "만화", "라이트노벨", "여행", "잡지", "어린이",
                        "유아", "전집", "청소년", "요리", "육아", "가정 살림", "건강 취미", "대학교재", "국어와 외국어", "IT 모바일", "수험서 자격증", "초등참고서", "중고등참고서"},
                {"ELT 사전", "문학 소설", "경제 경영", "인문 사회", "예술 대중문화", "취미 라이프스타일", "컴퓨터", "자연과학",
                        "대학교재 전문서", "해외잡지", "유아어린이청소년", "캐릭터북", "초등코스북", "학습서", "일본도서", "중국도서", "프랑스도서"},
                {"소설", "에세이", "경제/경영", "자기계발", "인문", "사회 정치", "역사", "종교", "만화", "자연과학", "외국어", "IT 모바일", "가정 살림",
                        "건강 취미", "해외원서"}
        };

        for (int i = 0; i < subCategoryNames.length; i++) {
            List<Category> subCategory = new ArrayList<>();
            subCategories.add(subCategory);
            for (int j = 0; j < subCategoryNames[i].length; j++) {
                subCategory.add(Category.builder()
                        .name(subCategoryNames[i][j])
                        .category(categories.get(i))
                        .build());
            }
        }

        for (Category category : categories) {
            em.persist(category);
        }

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            for (int j = 0; j < random.nextInt(5) + 1; j++) {
                int index = random.nextInt(subCategories.size());
                Category subCategory = subCategories.get(index).get(random.nextInt(subCategories.get(index).size()));
                if (book.getProductCategories().stream().anyMatch(cb -> cb.getCategory().equals(subCategory))) {
                    j--;
                } else {
                    em.persist(ProductCategory.builder()
                            .category(subCategory)
                            .product(book)
                            .build());
                }
            }
        }
    }

}
