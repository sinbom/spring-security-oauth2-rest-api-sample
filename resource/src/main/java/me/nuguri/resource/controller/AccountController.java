package me.nuguri.resource.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.resource.domain.AccountAdapter;
import me.nuguri.resource.common.Role;
import me.nuguri.resource.domain.Pagination;
import me.nuguri.resource.entity.Account;
import me.nuguri.resource.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    @GetMapping(value = "/api/v1/users", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> queryUsers(PagedResourcesAssembler<Account> assembler, Pageable pageable) {
        PagedModel<GetUsersResource> getUserResources = assembler.toModel(accountService.findAll(pageable),
                account -> new GetUsersResource(modelMapper.map(account, GetUserResponse.class)));
        getUserResources.add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
        return ResponseEntity.ok(getUserResources);
    }

    @GetMapping(value = "/api/v1/user/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        GetUserResource getUserResource = new GetUserResource(modelMapper.map(accountService.find(id), GetUserResponse.class));
        return ResponseEntity.ok(getUserResource);
    }

    @GetMapping(value = "/api/v1/user")
    public ResponseEntity<?> getMe(OAuth2Authentication authentication) {
        return ResponseEntity.ok(new GetMeResponse(authentication));
    }

    @PostMapping(value = "/api/v1/user", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> generateUser(@RequestBody GenerateUserRequest request) {
        GenerateUserResource generateUserResource = new GenerateUserResource(modelMapper.map(accountService.generate(modelMapper.map(request, Account.class)), GetUserResponse.class));
        return ResponseEntity.ok(generateUserResource);
    }

    @PatchMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, OAuth2Authentication authentication) {
        Account account = accountService.find(id);
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
        Account account = accountService.find(id);
        if (hasAuthority(account, authentication)) {
            modelMapper.map(request, account);
            return ResponseEntity.ok(new MergeUserResource(modelMapper.map(account, GetUserResponse.class)));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping(value = "/api/v1/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, OAuth2Authentication authentication) {
        Account account = accountService.find(id);
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
    public static class GetUserResponse {
        private Long id;
        private String email;
        private Set<Role> roles;
    }

    @Data
    public static class GetMeResponse {
        private String email;
        public GetMeResponse(OAuth2Authentication authentication) {
            this.email = authentication.getName();
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

    public static class GetUsersResource extends EntityModel<GetUserResponse> {
        public GetUsersResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("GET"));
            add(linkTo(methodOn(AccountController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GetUserResource extends EntityModel<GetUserResponse> {
        public GetUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("GET"));
            add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class GenerateUserResource extends EntityModel<GetUserResponse> {
        public GenerateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("POST"));
            add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class UpdateUserResource extends EntityModel<GetUserResponse> {
        public UpdateUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("PATCH"));
            add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class MergeUserResource extends EntityModel<GetUserResponse> {
        public MergeUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("PUT"));
            add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
            add(linkTo(methodOn(AccountController.class).deleteUser(content.getId(), null)).withRel("deleteUser").withType("DELETE"));
        }
    }

    public static class DeleteUserResource extends EntityModel<GetUserResponse> {
        public DeleteUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel().withType("DELETE"));
            add(linkTo(AccountController.class).slash("/docs/index.html").withRel("document"));
            add(linkTo(methodOn(AccountController.class).getUser(content.getId())).withRel("getUser").withType("GET"));
            add(linkTo(methodOn(AccountController.class).mergeUser(content.getId(), null, null)).withRel("mergeUser").withType("PUT"));
            add(linkTo(methodOn(AccountController.class).updateUser(content.getId(), null, null)).withRel("updateUser").withType("PATCH"));
        }
    }

}
