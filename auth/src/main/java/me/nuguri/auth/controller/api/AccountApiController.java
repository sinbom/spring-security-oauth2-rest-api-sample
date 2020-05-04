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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    private final PaginationValidator paginationValidator;

    private final ModelMapper modelMapper;

    private final AuthenticationManager authenticationManager;

    /**
     * API 방식 로그인
     * @param request username 이메일, password 비밀번호
     * @param session 세션
     * @return
     */
    @PostMapping(value = "/api/v1/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid username or password"));
        }
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return ResponseEntity.ok(new LoginResponse(session.getId()));
    }

    /**
     * 유저 정보 페이징 조회
     * @param assembler 페이징 리소스
     * @param pagination page 페이지 번호, size 페이지 당 갯수, sort 정렬(방식,기준)
     * @param errors 에러
     * @return
     */
    @GetMapping(value = "/api/v1/users", produces = MediaTypes.HAL_JSON_VALUE)
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

    @PostMapping(value = "/api/v1/user", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> generateUser(@RequestBody GenerateUserRequest request) {
        return ResponseEntity.ok(new GenerateUserResource(modelMapper.map(accountService.generate(modelMapper.map(request, Account.class)), GetUserResponse.class)));
    }

    @PatchMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, OAuth2Authentication authentication) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Account account = optionalAccount.get();
        if (hasAuthority(account, authentication)) {
            account.setPassword(StringUtils.isEmpty(request.getPassword()) ? account.getPassword() : request.getPassword());
            account.setRoles(request.getRoles().isEmpty() ? account.getRoles() : request.getRoles().stream().map(r -> Role.valueOf(r.toUpperCase())).collect(Collectors.toSet()));
            return ResponseEntity.ok(new UpdateUserResource(modelMapper.map(account, GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> mergeUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, OAuth2Authentication authentication) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Account account = optionalAccount.get();
        if (hasAuthority(account, authentication)) {
            modelMapper.map(request, account);
            return ResponseEntity.ok(new MergeUserResource(modelMapper.map(account, AccountApiController.GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, OAuth2Authentication authentication) {
        Optional<Account> optionalAccount = accountService.find(id);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Account account = optionalAccount.get();
        if (hasAuthority(account, authentication)) {
            accountService.delete(id);
            return ResponseEntity.ok(new DeleteUserResource(modelMapper.map(account, GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private boolean hasAuthority(Account account, OAuth2Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(r -> ("ROLE_" + Role.ADMIN).equals(r.getAuthority())) ||
                account.getEmail().equals(authentication.getName());
    }

    @Data
    public static class LoginResponse {
        private String sessionId;
        public LoginResponse(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    @Data
    public static class GenerateUserRequest {
        private String email;
        private String password;
        private Set<String> roles;
    }

    @Data
    public static class UpdateUserRequest {
        private String password;
        private Set<String> roles;
    }

    @Data
    public static class GetUserResponse {
        private Long id;
        private String email;
        private Set<Role> roles;
    }

    public static class GetUsersResource extends EntityModel<GetUserResponse> {
        public GetUsersResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GetUserResource extends EntityModel<GetUserResponse> {
        public GetUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GenerateUserResource extends EntityModel<GetUserResponse> {
        public GenerateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("POST"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class UpdateUserResource extends EntityModel<GetUserResponse> {
        public UpdateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class MergeUserResource extends EntityModel<GetUserResponse> {
        public MergeUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountApiController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class DeleteUserResource extends EntityModel<GetUserResponse> {
        public DeleteUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountApiController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(AccountApiController.class).slash(content.getId()).withSelfRel().withType("DELETE"));
            add(linkTo(methodOn(AccountApiController.class).getUser(content.getId(), null)).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountApiController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountApiController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
        }
    }
    
}
