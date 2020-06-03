package me.nuguri.resc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.common.entity.*;
import me.nuguri.common.enums.Gender;
import me.nuguri.resc.repository.CompanyRepository;
import me.nuguri.resc.repository.CreatorRepository;
import me.nuguri.resc.repository.CategoryRepository;
import me.nuguri.resc.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final CreatorRepository creatorRepository;

    private final CategoryRepository categoryRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final CompanyRepository companyRepository;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Override
    public void run(ApplicationArguments args) {
        if (profile.equals("local") && ddlAuto.equals("create")) {
            log.info("[log] [active profile is " + profile + "] => do persist test entities");
            Random random = new Random();
            List<Creator> creatorList = new ArrayList<>();
            String[] authorNames = {"홍길동", "아무개", "김똥개", "신나라", "박대기"};
            LocalDate[] authorBirth = {LocalDate.of(1946, 9, 17), LocalDate.of(1926, 5, 21)
                    , LocalDate.of(1996, 10, 25), LocalDate.of(1920, 6, 13), LocalDate.of(1965, 4, 2)};
            LocalDate[] authorDeath = {LocalDate.of(2018, 12, 1), LocalDate.of(2006, 4, 19)
                    , LocalDate.of(2020, 2, 14), LocalDate.of(1987, 7, 11), LocalDate.of(1999, 9, 12)};

            for (int i = 0; i < authorNames.length; i++) {
                Creator creator = new Creator();
                creator.setName(authorNames[i]);
                creator.setBirth(authorBirth[i]);
                creator.setDeath(authorDeath[i]);
                creator.setGender(i % 2 == 0 ? Gender.M : Gender.F);
                creatorList.add(creator);
            }

            creatorRepository.saveAll(creatorList);

            List<Book> bookList = new ArrayList<>();
            long min = LocalDate.of(1900, 1, 1).toEpochDay();
            long max = LocalDate.now().toEpochDay();
            for (int i = 0; i < 200; i++) {
                Book book = new Book();
                book.setName("책 " + i);
                book.setPublishDate(LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(min, max)));
                book.addCreator(creatorList.get(random.nextInt(creatorList.size())));
                Company company = new Company();
                company.setName("아무회사" + i);
                company.setEstablishDate(LocalDate.now());
                book.addCompany(company);
                bookList.add(book);

                companyRepository.save(company);
            }

            List<Category> majorCategoryList = new ArrayList<>();
            String[] majorCategoryNames = {"국내도서", "외국도서", "eBook"};

            for (String majorCategoryName : majorCategoryNames) {
                Category category = new Category();
                category.setName(majorCategoryName);
                majorCategoryList.add(category);
            }

            List<List<MinorCategory>> minorCategoryList = new ArrayList<>();
            String[][] minorCategoryNames = {
                    {"소설/시", "에세이", "인문", "역사", "예술", "종교", "사회", "과학", "경제/경영", "자기계발", "만화", "라이트노벨", "여행", "잡지", "어린이",
                            "유아", "전집", "청소년", "요리", "육아", "가정 살림", "건강 취미", "대학교재", "국어와 외국어", "IT 모바일", "수험서 자격증", "초등참고서", "중고등참고서"},
                    {"ELT 사전", "문학 소설", "경제 경영", "인문 사회", "예술 대중문화", "취미 라이프스타일", "컴퓨터", "자연과학",
                            "대학교재 전문서", "해외잡지", "유아어린이청소년", "캐릭터북", "초등코스북", "학습서", "일본도서", "중국도서", "프랑스도서"},
                    {"소설", "에세이", "경제/경영", "자기계발", "인문", "사회 정치", "역사", "종교", "만화", "자연과학", "외국어", "IT 모바일", "가정 살림",
                            "건강 취미", "해외원서"}
            };

            for (int i = 0; i < minorCategoryNames.length; i++) {
                List<MinorCategory> categoryList = new ArrayList<>();
                minorCategoryList.add(categoryList);
                for (int j = 0; j < minorCategoryNames[i].length; j++) {
                    MinorCategory category = new MinorCategory();
                    category.setName(minorCategoryNames[i][j]);
                    category.addMajorCategory(majorCategoryList.get(i));
                    categoryList.add(category);
                }
            }

            categoryRepository.saveAll(majorCategoryList);

            for (int i = 0; i < bookList.size(); i++) {
                Book book = bookList.get(i);
                for (int j = 0; j < random.nextInt(5) + 1; j++) {
                    int index = random.nextInt(minorCategoryList.size());
                    MinorCategory minorCategory = minorCategoryList.get(index).get(random.nextInt(minorCategoryList.get(index).size()));
                    if (book.getProductCategories().stream().anyMatch(cb -> cb.getCategory().equals(minorCategory))) {
                        j--;
                    } else {
                        ProductCategory productCategory = new ProductCategory();
                        productCategory.addMinorCategory(minorCategory);
                        productCategory.addProduct(book);
                        productCategoryRepository.save(productCategory);
                    }
                }
            }
        } else {
            log.info("[log] [active profile is " + profile + "] => do not persist test entities");
        }
    }
}
