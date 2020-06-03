package me.nuguri.common.support;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.common.entity.*;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public class EntityInitializer {

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void init(EntityManager em) {
        log.info("[log] EntityInitializer init entities");
        Account admin = Account.builder()
                .name("관리자")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("1234"))
                .gender(Gender.M)
                .address(new Address("경기도 과천시", "부림2길 76 2층", "13830"))
                .role(Role.ADMIN)
                .build();
        Account user = Account.builder()
                .name("사용자")
                .email("user@naver.com")
                .password(passwordEncoder.encode("1234"))
                .gender(Gender.F)
                .address(new Address("경기도 과천시", "부림2길 76 2층", "13830"))
                .role(Role.USER)
                .build();

        Client.builder()
                .clientId("nuguri")
                .clientSecret(passwordEncoder.encode("bom"))
                .resourceIds("account,nuguri")
                .scope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()))
                .grantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                        GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()))
                .redirectUri("http://localhost:9600/main")
                .authorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()))
                .account(admin)
                .build();

        Client.builder()
                .clientId("test")
                .clientSecret(passwordEncoder.encode("test"))
                .resourceIds("account")
                .scope(String.join(",", Scope.READ.toString()))
                .grantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.CLIENT_CREDENTIALS.toString()))
                .redirectUri("http://localhost:9600/main")
                .authorities(String.join(",", Role.USER.toString()))
                .account(user)
                .build();

        em.persist(user);
        em.persist(admin);

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
