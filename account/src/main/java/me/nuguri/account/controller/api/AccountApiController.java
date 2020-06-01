package me.nuguri.account.controller.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.account.annotation.HasAuthority;
import me.nuguri.account.dto.AccountSearchCondition;
import me.nuguri.account.exception.UserNotExistException;
import me.nuguri.account.service.AccountService;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.domain.Pagination;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Address;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Role;
import me.nuguri.common.validator.PaginationValidator;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;

@RestController
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    private final PaginationValidator paginationValidator;

    private final AccountValidator accountValidator;

    private final ModelMapper modelMapper;

    @GetMapping(
            value = "/api/v1/user/me",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> getMe(Principal principal) {
        try {
            String email = principal.getName();
            Account account = accountService.find(email);
            GetUserResponse getUserResponse = modelMapper.map(account, GetUserResponse.class);
            GetUserResource getUserResource = new GetUserResource(getUserResponse);
            return ResponseEntity.ok(getUserResource);
        } catch (UserNotExistException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(NOT_FOUND, "not exist account of token"));
        }
    }

    /**
     * 유저 정보 페이징 조회
     *
     * @param pageable page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors   에러
     * @return
     */
    /**
     * 유저 정보 페이징 조회
     * @param assembler 페이징 hateoas 리소스 생성 객체
     * @param pagination page 페이지, size 사이즈, sort 정렬
     * @param condition
     * @param errors 에러
     * @return
     */
    @GetMapping(
            value = "/api/v1/users",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )// clientHasRole은 접두어 ROLE_ 없을때 허가, 즉 client_credentails일 때 허가, 하지만 client detail service 직접 구현후 접두어 붙혀줄 수 있음
    @PreAuthorize("(hasRole('ADMIN') or #oauth2.clientHasRole('ADMIN')) and #oauth2.hasScope('read')")
    public ResponseEntity<?> queryUsers(PagedResourcesAssembler<Account> assembler, Pagination pagination,
                                        @Valid AccountSearchCondition condition, Errors errors) {
        paginationValidator.validate(pagination, Account.class, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid parameter value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Page<Account> page = accountService.pageByCondition(condition, pagination.getPageable());
        if (page.getNumberOfElements() < 1) {
            String message = page.getTotalElements() < 1 ? "content of all pages does not exist" : "content of current page does not exist";
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, message);
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
        PagedModel<QueryUserResource> getUserResources = assembler.toModel(page,
                account -> new QueryUserResource(modelMapper.map(account, GetUserResponse.class)));
        getUserResources.add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
        return ResponseEntity.ok(getUserResources);
    }

    /**
     * 유저 정보 조회
     *
     * @param id 식별키
     * @return
     */
    @GetMapping(
            value = "/api/v1/user/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("#oauth2.hasScope('read')")
    @HasAuthority
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            Account account = accountService.find(id);
            GetUserResponse getUserResponse = modelMapper.map(account, GetUserResponse.class);
            GetUserResource getUserResource = new GetUserResource(getUserResponse);
            return ResponseEntity.ok(getUserResource);
        } catch (UserNotExistException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist account of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 유저 정보 생성
     *
     * @param request email 이메일, password 비밀번호, roles 권한
     * @param errors  에러
     * @return
     */
    @PostMapping(
            value = "/api/v1/user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> generateUser(@RequestBody @Valid GenerateUserRequest request, Errors errors) {
        Account account = modelMapper.map(request, Account.class);
        accountValidator.validate(account, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (accountService.exist(request.getEmail())) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "email is already exist");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Account generate = accountService.generate(account);
        GetUserResponse getUserResponse = modelMapper.map(generate, GetUserResponse.class);
        GenerateUserResource generateUserResource = new GenerateUserResource(getUserResponse);
        return ResponseEntity
                .created(linkTo(methodOn(AccountApiController.class).generateUser(null, null)).toUri())
                .body(generateUserResource);
    }

    /**
     * 유저 정보 입력된 값만 변경
     *
     * @param id      식별키
     * @param request password 비밀번호, roles 권한
     * @param errors  에러
     * @return
     */
    @PatchMapping(
            value = "/api/v1/user/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("#oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, Errors errors) {
        Account account = modelMapper.map(request, Account.class);
        account.setId(id);
        accountValidator.validate(account, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            Account update = accountService.update(account);
            GetUserResponse getUserResponse = modelMapper.map(update, GetUserResponse.class);
            UpdateUserResource updateUserResource = new UpdateUserResource(getUserResponse);
            return ResponseEntity.ok(updateUserResource);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist account of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 유저 정보 전체 값 변경, 없는 유저의 경우 생성
     *
     * @param id      식별키
     * @param request password 비밀번호, roles 권한
     * @param errors  에러
     * @return
     */
    @PutMapping(
            value = "/api/v1/user/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("#oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> mergeUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request, Errors errors) {
        Account account = modelMapper.map(request, Account.class);
        account.setId(id);
        accountValidator.validate(account, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            Account merge = accountService.merge(account);
            GetUserResponse getUserResponse = modelMapper.map(merge, GetUserResponse.class);
            MergeUserResource mergeUserResource = new MergeUserResource(getUserResponse);
            return ResponseEntity.ok(mergeUserResource);
        } catch (UserNotExistException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist account of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 유저 정보 삭제
     *
     * @param id 식별키
     * @return
     */
    @DeleteMapping(
            value = "/api/v1/user/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("#oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            accountService.delete(id);
            DeleteUserResponse deleteUserResponse = new DeleteUserResponse(1);
            DeleteUserResource deleteUserResource = new DeleteUserResource(deleteUserResponse, id);
            return ResponseEntity.ok(deleteUserResource);
        } catch (UserNotExistException e) {
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist account of id");
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    // ==========================================================================================================================================
    // Domain
    @Getter
    @Setter
    public static class GenerateUserRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotNull
        private Gender gender;
        @NotNull
        private Address address;
        @NotNull
        private Role role;
    }

    @Getter
    @Setter
    public static class UpdateUserRequest {
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotNull
        private Gender gender;
        @NotNull
        private Address address;
        @NotNull
        private Role role;
    }

    @Getter
    @Setter
    public static class GetUserResponse {
        private Long id;
        private String email;
        private String name;
        private Gender gender;
        private Address address;
        private Role role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DeleteUserResponse {
        private long count;
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Resource
    public static class QueryUsersResource extends EntityModel<GetUserResponse> {
        public QueryUsersResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class QueryUserResource extends EntityModel<GetUserResponse> {
        public QueryUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GetUserResource extends EntityModel<GetUserResponse> {
        public GetUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GenerateUserResource extends EntityModel<GetUserResponse> {
        public GenerateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).generateUser(null, null)).withSelfRel().withType("POST"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class UpdateUserResource extends EntityModel<GetUserResponse> {
        public UpdateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withSelfRel().withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class MergeUserResource extends EntityModel<GetUserResponse> {
        public MergeUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withSelfRel().withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId())).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class DeleteUserResource extends EntityModel<DeleteUserResponse> {
        public DeleteUserResource(DeleteUserResponse content, Long id, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(id)).withSelfRel().withType("DELETE"));
        }
    }

    public static class DeleteUsersResource extends EntityModel<DeleteUserResponse> {
        public DeleteUsersResource(DeleteUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/account.html").withRel("document"));
        }
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Validator
    @Component
    public static class AccountValidator {
        /**
         * Account 도메인 condition 값 중 이메일, 비밀번호, 주소 검증
         *
         * @param request email 이메일, password 비밀번호, address 주소(시도 + 도로명 + 우편번호)
         * @param errors  에러
         */
        public void validate(Account request, Errors errors) {
            String email = request.getEmail();
            String password = request.getPassword();
            Address address = request.getAddress();
            if (hasText(email)) {
                if (!email.matches("^[a-zA-Z0-9_-]{5,15}@[a-zA-Z0-9-]{1,10}\\.[a-zA-Z]{2,6}$")) {
                    errors.rejectValue("email", "wrongValue", "email is wrong ex) [alphabet or number 10~15]@[alphabet or number 1~10].[alphabet 2~6]");
                }
            }
            if (hasText(password)) {
                if (!password.matches("^.{5,15}$")) {
                    errors.rejectValue("password", "wrongValue", "password is wrong, any character from 5 to 15");
                }
            }
            if (address != null) {
                String city = address.getCity();
                String street = address.getStreet();
                String zipCode = address.getZipCode();
                if (isEmpty(city) || isEmpty(street) || isEmpty(zipCode)) {
                    errors.reject("wrongValue", "address(city, street, zipCode) field is blank");
                }
            }
        }
    }
    // ==========================================================================================================================================

}
