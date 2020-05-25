package me.nuguri.resc.controller.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.domain.Pagination;
import me.nuguri.common.domain.PaginationResource;
import me.nuguri.common.validator.PaginationValidator;
import me.nuguri.resc.entity.Author;
import me.nuguri.resc.entity.Book;
import me.nuguri.resc.service.AuthorService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class AuthorApiController {

    private final AuthorService authorService;

    private final PaginationValidator paginationValidator;

    private final ModelMapper modelMapper;

    private final AuthorValidator authorValidator;

    /**
     * 저자 정보 페이징 조회
     * @param pagination page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors 에러
     * @return
     */
    @GetMapping(value = "/api/v1/authors", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> queryAuthors(Pagination pagination, Errors errors) {
        paginationValidator.validate(pagination, Author.class, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid parameters", errors));
        }
        Page<Author> page = authorService.findAll(pagination.getPageable());
        if (page.getNumberOfElements() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND, page.getTotalElements() < 1 ? "content of all pages does not exist" : "content of current page does not exist"));
        }
        PaginationResource<QueryAuthorsResource> queryAuthorsResources = new PaginationResource<>(page, author -> new QueryAuthorsResource(new GetAuthorResponse(author)));
        queryAuthorsResources.addPaginationLink(pagination, linkTo(methodOn(AuthorApiController.class).queryAuthors(null, null)));
        queryAuthorsResources.add(linkTo(AuthorApiController.class).slash("/docs/author.html").withRel("document"));
        return ResponseEntity.ok(queryAuthorsResources);
    }

    /**
     * 저자 정보 조회
     * @param id 식별키
     * @return
     */
    @GetMapping("/api/v1/author/{id}")
    public ResponseEntity<?> getAuthor(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new GetAuthorResponse(authorService.find(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "not exist id of author"));
        }
    }

    /**
     * 저자 정보 생성
     * @param request name 이름, birth 출생날짜, death 사망 날짜
     * @param errors 에러
     * @return
     */
    @PostMapping("/api/v1/author")
    public ResponseEntity<?> generateAuthor(@RequestBody @Valid GenerateAuthorRequest request, Errors errors) {
        Author author = modelMapper.map(request, Author.class);
        authorValidator.validate(author, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors));
        }
        return ResponseEntity.created(linkTo(methodOn(AuthorApiController.class)
                .generateAuthor(null, null))
                .toUri())
                .body(new GetAuthorResponse(authorService.generate(author)));
    }

    @PatchMapping("/api/v1/author/{id}")
    public ResponseEntity<?> updateAuthor() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/v1/author/{id}")
    public ResponseEntity<?> mergeAuthor() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/author/{id}")
    public ResponseEntity<?> deleteAuthor() {
        return ResponseEntity.ok().build();
    }

    // ==========================================================================================================================================
    // Resource
    public static class QueryAuthorsResource extends EntityModel<GetAuthorResponse> {
        public QueryAuthorsResource(GetAuthorResponse content, Link... links) {
            super(content, links);
        }
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Domain
    @Getter @Setter
    public static class GetAuthorResponse {
        @Getter @Setter
        private static class GetBookResponse {
            private Long id;
            private String name;
            private LocalDate pubDate;
            public GetBookResponse(Book book) {
                this.id = book.getId();
                this.name = book.getName();
                this.pubDate = book.getPubDate();
            }
        }
        private Long id;
        private String name;
        private LocalDate birth;
        private LocalDate death;
        private List<GetBookResponse> books = new ArrayList<>();

        public GetAuthorResponse(Author author) {
            this.id = author.getId();
            this.name = author.getName();
            this.birth = author.getBirth();
            this.death = author.getDeath();
            this.books = author.getBooks()
                    .stream()
                    .map(GetBookResponse::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter @Setter
    public static class GenerateAuthorRequest {
        @NotBlank
        private String name;
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birth;
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate death;
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Validator
    @Component
    public static class AuthorValidator {
        /**
         * Author 도메인 값 중 생년날짜, 사망날짜 검증
         * @param author birth 생년날짜, death 사망날짜
         * @param errors 에러
         */
        public void validate(Author author, Errors errors) {
            LocalDate birth = author.getBirth();
            LocalDate death = author.getDeath();
            if (birth != null && death != null) {
                if (death.isBefore(birth)) {
                    errors.reject("wrongValue", "birth is must be before death");
                }
            }
        }
    }
    // ==========================================================================================================================================
}
