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
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class CreatorApiController {

    private final CreatorService creatorService;

    private final PaginationValidator paginationValidator;

    private final ModelMapper modelMapper;

    private final CreatorValidator creatorValidator;

    /**
     * 저자 정보 페이징 조회
     *
     * @param pagination page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors     에러
     * @return 응답
     */
    @GetMapping(value = "/api/v1/creators", produces = HAL_JSON_VALUE)
    public ResponseEntity<?> queryCreators(CreatorSearchCondition condition, Pagination pagination, Errors errors) {
        paginationValidator.validate(pagination, Creator.class, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid parameters", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Page<Creator> page = creatorService.pagingWithCondition(condition, pagination.getPageable());
        if (page.getNumberOfElements() < 1) {
            String message = page.getTotalElements() < 1 ? "content of all pages does not exist" : "content of current page does not exist";
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, message);
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
        PaginationResource<QueryCreatorsResource> queryCreatorsResources = new PaginationResource<>(page,
                creator -> new QueryCreatorsResource(new GetCreatorResponse(creator)));

        WebMvcLinkBuilder builder = linkTo(methodOn(CreatorApiController.class).queryCreators(null, null, null));
        queryCreatorsResources.addPaginationLink(builder, pagination, condition.paramsToMap());
        queryCreatorsResources.add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
        return ResponseEntity.ok(queryCreatorsResources);
    }

    /**
     * 저자 정보 조회
     *
     * @param id 식별키
     * @return 응답
     */
    @GetMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> getCreator(@PathVariable Long id) {
        try {
            Creator creator = creatorService.find(id);
            GetCreatorResponse getCreatorResponse = new GetCreatorResponse(creator);
            GetCreatorResource getCreatorResource = new GetCreatorResource(getCreatorResponse);
            return ResponseEntity.ok(getCreatorResource);
        } catch (NoSuchElementException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist id of creator");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 저자 정보 생성
     *
     * @param request name 이름, gender 성별, birth 출생날짜, death 사망 날짜
     * @param errors  에러
     * @return 응답
     */
    @PostMapping("/api/v1/creator")
    public ResponseEntity<?> generateCreator(@RequestBody @Valid GenerateCreatorRequest request, Errors errors) {
        Creator creator = modelMapper.map(request, Creator.class);
        creatorValidator.validate(creator, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Creator generate = creatorService.generate(creator);
        GetCreatorResponse getCreatorResponse = new GetCreatorResponse(generate);
        GetCreatorResource getCreatorResource = new GetCreatorResource(getCreatorResponse);
        return ResponseEntity
                .created(linkTo(methodOn(CreatorApiController.class).generateCreator(null, null)).toUri())
                .body(getCreatorResource);
    }

    /**
     * 저자 정보 수정
     *
     * @param id      식별키
     * @param request name 이름, gender 성별, birth 출생날짜, death 사망 날짜
     * @param errors  에러
     * @return
     */
    @PatchMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> updateCreator(@PathVariable Long id, @RequestBody GenerateCreatorRequest request, Errors errors) {
        Creator creator = modelMapper.map(request, Creator.class);
        creator.setId(id);
        creatorValidator.validate(creator, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Creator update = creatorService.update(creator);
            GetCreatorResponse getCreatorResponse = new GetCreatorResponse(update);
            GetCreatorResource getCreatorResource = new GetCreatorResource(getCreatorResponse);
            return ResponseEntity.ok(getCreatorResource);
        } catch (NoSuchElementException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist element of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 저자 정보 병합
     *
     * @param id      식별키
     * @param request name 이름, gender 성별, birth 출생날짜, death 사망 날짜
     * @param errors  에러
     * @return
     */
    @PutMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> mergeCreator(@PathVariable Long id, @RequestBody @Valid GenerateCreatorRequest request, Errors errors) {
        Creator creator = modelMapper.map(request, Creator.class);
        creatorValidator.validate(creator, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        boolean isExist = creatorService.exist(id);
        Creator merge;
        ResponseEntity.BodyBuilder responseEntity;
        if (isExist) {
            creator.setId(id);
            merge = creatorService.merge(creator);
            responseEntity = ResponseEntity.ok();
        } else {
            merge = creatorService.generate(creator);
            responseEntity = ResponseEntity.created(linkTo(methodOn(CreatorApiController.class).mergeCreator(id, null, null)).toUri());
        }
        GetCreatorResponse getCreatorResponse = new GetCreatorResponse(merge);
        GetCreatorResource getCreatorResource = new GetCreatorResource(getCreatorResponse);
        return responseEntity.body(getCreatorResource);
    }

    @DeleteMapping("/api/v1/creator/{id}")
    public ResponseEntity<?> deleteCreator(@PathVariable Long id) {
        try {
            creatorService.delete(id);
            DeleteCreatorResource deleteCreatorResource = new DeleteCreatorResource(id);
            return ResponseEntity.ok(deleteCreatorResource);
        } catch (NoSuchElementException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist element of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/api/v1/creators")
    public ResponseEntity<?> deleteCreators(@RequestBody @Valid CreatorApiController.DeleteCreatorsRequest request, Errors errors) {
        creatorValidator.validate(request.ids, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            creatorService.deleteInBatch(request.ids);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist element of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    // ==========================================================================================================================================
    // Resource
    public static class QueryCreatorsResource extends EntityModel<GetCreatorResponse> {
        public QueryCreatorsResource(GetCreatorResponse content, Link... links) {
            super(content, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("getCreator").withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).updateCreator(content.getId(), null, null)).withRel("updateCreator").withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).mergeCreator(content.getId(), null, null)).withRel("mergeCreator").withType("PUT"));
            add(linkTo(methodOn(CreatorApiController.class).deleteCreator(content.getId())).withRel("deleteCreator").withType("DELETE"));
        }
    }

    public static class GetCreatorResource extends EntityModel<GetCreatorResponse> {
        public GetCreatorResource(GetCreatorResponse content, Link... links) {
            super(content, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withSelfRel().withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).updateCreator(content.getId(), null, null)).withRel("updateCreator").withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).mergeCreator(content.getId(), null, null)).withRel("mergeCreator").withType("PUT"));
            add(linkTo(methodOn(CreatorApiController.class).deleteCreator(content.getId())).withRel("deleteCreator").withType("DELETE"));
        }
    }

    public static class UpdateCreatorResource extends EntityModel<GetCreatorResponse> {
        public UpdateCreatorResource(GetCreatorResponse content, Link... links) {
            super(content, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).updateCreator(content.getId(), null, null)).withSelfRel().withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("getCreator").withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).mergeCreator(content.getId(), null, null)).withRel("mergeCreator").withType("PUT"));
            add(linkTo(methodOn(CreatorApiController.class).deleteCreator(content.getId())).withRel("deleteCreator").withType("DELETE"));
        }
    }

    public static class MergeCreatorResource extends EntityModel<GetCreatorResponse> {
        public MergeCreatorResource(GetCreatorResponse content, Link... links) {
            super(content, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).mergeCreator(content.getId(), null, null)).withSelfRel().withType("PUT"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(content.getId())).withRel("getCreator").withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).updateCreator(content.getId(), null, null)).withRel("updateCreator").withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).deleteCreator(content.getId())).withRel("deleteCreator").withType("DELETE"));
        }
    }

    public static class DeleteCreatorResource extends EntityModel<Long> {
        public DeleteCreatorResource(Long id, Link... links) {
            super(id, links);
            add(linkTo(CreatorApiController.class).slash("/docs/creator.html").withRel("document"));
            add(linkTo(methodOn(CreatorApiController.class).deleteCreator(id)).withSelfRel().withType("DELETE"));
            add(linkTo(methodOn(CreatorApiController.class).getCreator(id)).withRel("getCreator").withType("GET"));
            add(linkTo(methodOn(CreatorApiController.class).updateCreator(id, null, null)).withRel("updateCreator").withType("PATCH"));
            add(linkTo(methodOn(CreatorApiController.class).mergeCreator(id, null, null)).withRel("mergeCreator").withType("PUT"));
        }
    }


    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Domain
    @Getter
    @Setter
    public static class GetCreatorResponse {
        @Getter
        @Setter
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

    @Getter
    @Setter
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

    @Getter
    @Setter
    public static class DeleteCreatorsRequest {
        @NotEmpty
        private List<Long> ids;
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Validator
    @Component
    public static class CreatorValidator {
        /**
         * creator 도메인 값 중 생년날짜, 사망날짜 검증
         *
         * @param creator birth 생년날짜, death 사망날짜
         * @param errors  에러
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

        /**
         * 식별키가 0이상의 정수인지 검증
         *
         * @param ids    식별키
         * @param errors 에러
         */
        public void validate(List<Long> ids, Errors errors) {
            boolean isAnyLowerThanZero = ids.stream().anyMatch(id -> id < 1);
            if (isAnyLowerThanZero) {
                errors.rejectValue("ids", "wrongValue", "id is must be greater than zero");
            }
        }
    }
    // ==========================================================================================================================================
}
