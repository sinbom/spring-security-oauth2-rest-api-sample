package me.nuguri.resource;

import lombok.RequiredArgsConstructor;
import me.nuguri.resource.entity.*;
import me.nuguri.resource.entity.embedded.CategoryBookId;
import me.nuguri.resource.repository.*;
import org.apache.tomcat.jni.Local;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final MajorCategoryRepository majorCategoryRepository;

    private final MinorCategoryRepository minorCategoryRepository;

    private final CategoryBookRepository categoryBookRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Random random = new Random();
        List<Author> authorList = new ArrayList<>();
        String[] authorNames = {"홍길동", "아무개", "김똥개", "신나라", "박대기"};
        LocalDate[] authorBirth = {LocalDate.of(1946, 9, 17), LocalDate.of(1926, 5, 21)
        , LocalDate.of(1996, 10, 25), LocalDate.of(1920, 6, 13), LocalDate.of(1965, 4,2)};
        LocalDate[] authorDeath = {LocalDate.of(2018, 12, 1), LocalDate.of(2006, 4, 19)
        , LocalDate.of(2020, 2, 14), LocalDate.of(1987, 7, 11), LocalDate.of(1999, 9, 12)};

        for (int i = 0; i < authorNames.length; i++) {
            Author author = Author.builder()
                    .name(authorNames[i])
                    .birth(authorBirth[i])
                    .death(authorDeath[i])
                    .books(new ArrayList<>())
                    .build();
            authorRepository.save(author);
            authorList.add(author);
        }

        List<Book> bookList =  new ArrayList<>();
        long min = LocalDate.of(1900, 1, 1).toEpochDay();
        long max = LocalDate.now().toEpochDay();
        for (int i = 0; i < 200; i++) {
            Book book = Book.builder()
                    .name(UUID.randomUUID().toString())
                    .pubDate(LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(min, max)))
                    .categoryBooks(new ArrayList<>())
                    .build();
            book.addAuthor(authorList.get(random.nextInt(authorList.size())));
            bookList.add(book);
            bookRepository.save(book);
        }

        List<MajorCategory> majorCategoryList = new ArrayList<>();
        String[] majorCategoryNames= {"국내도서", "외국도서", "eBook"};

        for (String majorCategoryName : majorCategoryNames) {
            MajorCategory category = MajorCategory.builder()
                    .name(majorCategoryName)
                    .minorCategories(new ArrayList<>())
                    .build();
            majorCategoryList.add(category);
            majorCategoryRepository.save(category);

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
                MinorCategory minorCategory = MinorCategory.builder()
                        .name(minorCategoryNames[i][j])
                        .categoryBooks(new ArrayList<>())
                        .build();
                minorCategory.addMajorCategory(majorCategoryList.get(i));
                categoryList.add(minorCategory);
                minorCategoryRepository.save(minorCategory);
            }
        }

        for (int i = 0; i < bookList.size(); i++) {
            Book book = bookList.get(i);
            for (int j = 0; j < random.nextInt(5) + 1; j++) {
                int index = random.nextInt(minorCategoryList.size());
                MinorCategory minorCategory = minorCategoryList.get(index).get(random.nextInt(minorCategoryList.get(index).size()));
                if (book.getCategoryBooks().stream().anyMatch(cb -> cb.getCategory().equals(minorCategory))) {
                    j--;
                } else {
                    CategoryBook categoryBook = new CategoryBook();
                    categoryBook.addMinorCategory(minorCategory);
                    categoryBook.addBook(book);
                    categoryBookRepository.save(categoryBook);
                }
            }
        }

    }
}
