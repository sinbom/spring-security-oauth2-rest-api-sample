package me.nuguri.auth.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.auth.annotation.AuthenticationUser;
import me.nuguri.auth.domain.ErrorResponse;
import me.nuguri.auth.domain.LoginRequest;
import me.nuguri.auth.domain.Pagination;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.service.AccountService;
import me.nuguri.auth.validator.PaginationValidator;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Optional;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@Transactional
public class AccountApiController {

    private final AccountService accountService;

    private final PaginationValidator paginationValidator;

    private final AccountValidator accountValidator;

    private final ModelMapper modelMapper;

    /**
     * API 방식 로그인
     * @param request username 이메일, password 비밀번호
     * @param errors 에러
     * @return
     */
    @PostMapping(value = "/api/v1/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, Errors errors) {
        accountValidator.validate(request, errors);
        try {
            return ResponseEntity.ok(new LoginResponse(accountService.login(request.getUsername(), request.getPassword())));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid username or password"));
        }
    }

    /**
     * 유저 정보 페이징 조회
     * @param assembler 페이징 리소스
     * @param pagination page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors 에러
     * @return
     */
    @GetMapping(value = "/api/v1/users", produces = MediaTypes.HAL_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<?> queryUsers(PagedResourcesAssembler<Account> assembler, Pagination pagination, Errors errors) {
        paginationValidator.validate(pagination, Account.class, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid parameter value", errors));
        }
        PagedModel<GetUsersResource> getUserResources = assembler.toModel(accountService.findAll(pagination.getPageable()),
                account -> new GetUsersResource(modelMapper.map(account, GetUserResponse.class)));
        getUserResources.add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
        return ResponseEntity.ok(getUserResources);
    }

    /**
     * 유저 정보 조회
     * @param id 식별키
     * @param account 현재 로그인 된 계정(세션)
     * @return
     */
    @GetMapping(value = "/api/v1/user/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUser(@PathVariable Long id, @AuthenticationUser Account account) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "not exist account of id"));
        }
        if (account.getId().equals(id) || account.getRoles().stream().anyMatch(r -> r.equals(Role.ADMIN))) {
            return ResponseEntity.ok(new GetUserResource(modelMapper.map(account, GetUserResponse.class)));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(HttpStatus.FORBIDDEN, "have no authority"));
    }

    /**
     * 유저 정보 생성
     * @param request email 이메일, password 비밀번호, roles 권한
     * @param errors 에러
     * @return
     */
    @PostMapping(value = "/api/v1/user", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> generateUser(@RequestBody @Valid GenerateUserRequest request, Errors errors) {
        accountValidator.validate(request, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid body value", errors));
        }
        Optional<Account> optionalAccount = accountService.find(request.getEmail());
        if (optionalAccount.isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "email is already exist"));
        }
        return ResponseEntity.created(linkTo(methodOn(AccountApiController.class)
                .generateUser(null, null))
                .toUri())
                .body(new GenerateUserResource(modelMapper.map(accountService.generate(modelMapper.map(request, Account.class)), GetUserResponse.class)));
    }

    /**
     * 유저 정보 입력된 값만 변경
     * @param id 식별키
     * @param request password 비밀번호, roles 권한
     * @param errors 에러
     * @param account 현재 로그인 된 계정(세션)
     * @return
     */
    @PatchMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request, Errors errors, @AuthenticationUser Account account) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (account.getId().equals(id) || account.getRoles().stream().anyMatch(r -> r.equals(Role.ADMIN))) {
            account.setPassword(StringUtils.isEmpty(request.getPassword()) ? account.getPassword() : request.getPassword());
            account.setRoles(request.getRoles().isEmpty() ? account.getRoles() : request.getRoles());
            return ResponseEntity.ok(new UpdateUserResource(modelMapper.map(account, GetUserResponse.class)));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(HttpStatus.FORBIDDEN, "have no authority"));
    }

    @PutMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> mergeUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, @AuthenticationUser Account account) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Account account2 = optionalAccount.get();
        if (true) {
            modelMapper.map(request, account2);
            return ResponseEntity.ok(new MergeUserResource(modelMapper.map(account, GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationUser Account account) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Account account2 = optionalAccount.get();
        if (true) {
            accountService.delete(id);
            return ResponseEntity.ok(new DeleteUserResource(modelMapper.map(account2, GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // ==========================================================================================================================================
    // Domain
    @Data
    public static class LoginResponse {
        private String sessionId;
        public LoginResponse(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    @Data
    public static class GenerateUserRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotEmpty
        private Set<Role> roles;
    }

    @Data
    public static class UpdateUserRequest {
        @NotBlank
        private String password;
        @NotEmpty
        private Set<Role> roles;
    }

    @Data
    public static class GetUserResponse {
        private Long id;
        private String email;
        private Set<Role> roles;
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Resource
    public static class GetUsersResource extends EntityModel<GetUserResponse> {
        public GetUsersResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(methodOn(AccountApiController.class).queryUsers(null, null, null)).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GetUserResource extends EntityModel<GetUserResponse> {
        public GetUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GenerateUserResource extends EntityModel<GetUserResponse> {
        public GenerateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).generateUser(null, null)).withSelfRel().withType("POST"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class UpdateUserResource extends EntityModel<GetUserResponse> {
        public UpdateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withSelfRel().withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class MergeUserResource extends EntityModel<GetUserResponse> {
        public MergeUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withSelfRel().withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class DeleteUserResource extends EntityModel<GetUserResponse> {
        public DeleteUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withSelfRel().withType("DELETE"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
        }
    }
    // ==========================================================================================================================================

    // ==========================================================================================================================================
    // Validator
    @Component
    public static class AccountValidator {
        public void validate(GenerateUserRequest request, Errors errors) {
            String emailPattern = "^[a-zA-Z0-9_-]{5,15}@[a-zA-Z0-9-]{1,10}\\.[a-zA-Z]{2,6}$";
            if (!request.getEmail().matches(emailPattern)) {
                errors.rejectValue("email", "wrongValue", "email is wrong ex) [alphabet or number 10~15]@[alphabet or number 1~10].[alphabet 2~6]");
            }
        }

        public void validate(LoginRequest request, Errors errors) {
        }
    }
    // ==========================================================================================================================================
    
}