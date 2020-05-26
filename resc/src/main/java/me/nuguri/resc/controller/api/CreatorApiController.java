package me.nuguri.resc.controller.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.domain.Pagination;
import me.nuguri.common.domain.PaginationResource;
import me.nuguri.common.validator.PaginationValidator;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Book;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.enums.Gender;
import me.nuguri.resc.service.CreatorService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class CreatorApiController {

    private final CreatorService creatorService;

    private final PaginationValidator paginationValidator;

    private final ModelMapper modelMapper;

    private final CreatorValidator creatorValidator;

    /**
     * 저자 정보 페이징 조회
     * @param pagination page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors 에러
     * @return
     */
    @GetMapping(value = "/api/v1/creators", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> queryCreators(CreatorSearchCondition condition, Pagination pagination, Errors errors) {
        paginationValidator.validate(pagination, Creator.class, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid parameters", errors));
        }
        Page<Creator> page = creatorService.pagingWithCondition(condition, pagination.getPageable());
        if (page.getNumberOfElements() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND, page.getTotalElements() < 1 ? "content of all pages does not exist" : "content of current page does not exist"));
        }
        PaginationResource<QueryCreatorsResource> queryCreatorsResources = new PaginationResource<>(page, creator -> new QueryCreatorsResource(new GetCreatorResponse(creator)));
        queryCreatorsResources.addPaginationLink(linkTo(methodOn(CreatorApiController.class).queryCreators(null, null, null)), pagination, condition.paramsToMap());
        queryCreatorsResources.add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
        return ResponseEntity.ok(queryCreatorsResources);
    }

    /**
     * 저자 정보 조회
     * @param id 식별키
     * @return
     */
    @GetMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> getCreator(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new GetCreatorResponse(creatorService.find(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "not exist id of creator"));
        }
    }

    /**
     * 저자 정보 생성
     * @param request name 이름, birth 출생날짜, death 사망 날짜
     * @param errors 에러
     * @return
     */
    @PostMapping("/api/v1/creator")
    public ResponseEntity<?> generateCreator(@RequestBody @Valid GenerateCreatorRequest request, Errors errors) {
        Creator creator = modelMapper.map(request, Creator.class);
        creatorValidator.validate(creator, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors));
        }
        return ResponseEntity.created(linkTo(methodOn(CreatorApiController.class)
                .generateCreator(null, null))
                .toUri())
                .body(new GetCreatorResource(new GetCreatorResponse(creatorService.generate(creator))));
    }

    @PatchMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> updateCreator(@PathVariable Long id, @RequestBody GenerateCreatorRequest request, Errors errors) {
        Creator creator = modelMapper.map(request, Creator.class);
        creator.setId(id);
        creatorValidator.validate(creator, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors));
        }
        try {
            return ResponseEntity.ok(new GetCreatorResource(new GetCreatorResponse(creatorService.update(creator))));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "not exist element of id"));
        }
    }

    @PutMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> mergeCreator() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> deleteCreator() {
        return ResponseEntity.ok().build();
    }

    // ==========================================================================================================================================
    // Resource
    public static class QueryCreatorsResource extends EntityModel<GetCreatorResponse> {
        public QueryCreatorsResource(GetCreatorResponse content, Link... links) {
            super(content, links);
        }
    }

    public static class GetCreatorResource extends EntityModel<GetCreatorResponse> {
        public GetCreatorResource(GetCreatorResponse content, Link... links) {
            super(content, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withSelfRel().withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("updateCreator").withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("mergeCreator").withType("PUT"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("deleteCreator").withType("DELETE"));
        }
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Domain
    @Getter @Setter
    public static class GetCreatorResponse {
        @Getter @Setter
        private static class GetBookResponse {
            private Long id;
            private String name;
            private LocalDate pubDate;
            public GetBookResponse(Book book) {
                this.id = book.getId();
                this.name = book.getName();
                this.pubDate = book.getPublishDate();
            }
        }
        private Long id;
        private String name;
        private LocalDate birth;
        private LocalDate death;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<GetBookResponse> books;

        public GetCreatorResponse(Creator creator) {
            this.id = creator.getId();
            this.name = creator.getName();
            this.birth = creator.getBirth();
            this.death = creator.getDeath();
            this.books = creator.getProducts()
                    .stream()
                    .map(p -> new GetBookResponse((Book) p))
                    .collect(Collectors.toList());
        }
    }

    @Getter @Setter
    public static class GenerateCreatorRequest {
        @NotBlank
        private String name;
        @NotNull
        private Gender gender;
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
    public static class CreatorValidator {
        /**
         * creator 도메인 값 중 생년날짜, 사망날짜 검증
         * @param creator birth 생년날짜, death 사망날짜
         * @param errors 에러
         */
        public void validate(Creator creator, Errors errors) {
            LocalDate birth = creator.getBirth();
            LocalDate death = creator.getDeath();
            if (birth != null && death != null) {
                if (death.isBefore(birth)) {
                    errors.reject("wrongValue", "birth is must be before death");
                }
            }
        }
    }
    // ==========================================================================================================================================
}
