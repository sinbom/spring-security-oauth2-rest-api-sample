package me.nuguri.resource.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.resource.common.Role;
import me.nuguri.resource.entity.Account;
import me.nuguri.resource.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final ModelMapper modelMapper;


    @GetMapping(value = "/api/v1/user", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<GetUserResource>> queryUsers(PagedResourcesAssembler<Account> assembler, Pageable pageable) {
        PagedModel<GetUserResource> getUserResources = assembler.toModel(accountService.findAll(pageable), account -> new GetUserResource(modelMapper.map(account, GetUserResponse.class)));
        return ResponseEntity.ok(getUserResources);
    }

    @GetMapping(value = "/api/v1/user/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<GetUserResource> getUser(@PathVariable Long id) {
        GetUserResource getUserResource = new GetUserResource(modelMapper.map(accountService.find(id), GetUserResponse.class));
        return ResponseEntity.ok(getUserResource);
    }

    @Data
    public static class GetUserResponse {
        private Long id;
        private String email;
        private Set<Role> roles;
    }

    public static class GetUserResource extends EntityModel<GetUserResponse> {
        public GetUserResource(GetUserResponse content, Link... links) {
            super(content, links);
            add(linkTo(AccountController.class).slash(content.getId()).withSelfRel());
        }
    }

}
