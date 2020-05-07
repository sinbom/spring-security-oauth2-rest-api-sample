package me.nuguri.resource.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.resource.domain.ErrorResponse;
import me.nuguri.resource.domain.Pagination;
import me.nuguri.resource.entity.Author;
import me.nuguri.resource.entity.Book;
import me.nuguri.resource.service.AuthorService;
import me.nuguri.resource.validator.PaginationValidator;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
public class AuthorApiController {

    private final AuthorService authorService;

    private final PaginationValidator paginationValidator;

    private final ModelMapper modelMapper;

    @GetMapping(value = "/api/v1/authors", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> queryAuthors(PagedResourcesAssembler<Author> assembler, Pagination pagination, Errors errors) {
        paginationValidator.validate(pagination, Author.class, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid parameters", errors));
        }
        PagedModel<QueryAuthorsResource> queryAuthorsResources = assembler.toModel(authorService.findAll(pagination.getPageable()),
                author -> new QueryAuthorsResource(new GetAuthorResponse(author)));
        queryAuthorsResources.add(linkTo(AuthorApiController.class).slash("/docs/author.html").withRel("document"));
        return ResponseEntity.ok(queryAuthorsResources);
    }

    public static class QueryAuthorsResource extends EntityModel<GetAuthorResponse> {
        public QueryAuthorsResource(GetAuthorResponse content, Link... links) {
            super(content, links);
        }
    }

    @Data
    public static class GetAuthorResponse {
        @Data
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

}
