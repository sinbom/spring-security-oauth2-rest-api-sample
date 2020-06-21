package me.nuguri.account.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Roles;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class IndexApiController {

    private final AccountRepository accountRepository;

    @GetMapping(
            value = "/api/v1/index",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> index(Principal principal) {
        IndexResource indexResource;
        if (principal != null) {
            String email = principal.getName();
            Optional<Account> opAccount = accountRepository.findByEmail(email);
            Account account = opAccount.orElse(null);
            indexResource = new IndexResource(account);
        } else {
            indexResource = new IndexResource(null);
        }
        return ResponseEntity.ok(indexResource);
    }

    @Getter
    @Setter
    public static class IndexResource {
        private List<Link> _links = new ArrayList<>();

        public IndexResource(Account account) {
            _links.add(linkTo(IndexApiController.class).slash("/docs/index.html").withRel("document"));
            _links.add(linkTo(IndexApiController.class).slash("/docs/account.html").withRel("document"));
            _links.add(linkTo(methodOn(IndexApiController.class).index(null)).withSelfRel());
            _links.add(linkTo(methodOn(AccountApiController.class).generateUser(null, null)).withRel("generateUser").withType("POST"));
            if (account != null) {
                if (account.getRoles().equals(Roles.ADMIN)) {
                    _links.add(linkTo(methodOn(AccountApiController.class).queryUsers(null, null, null)).withRel("queryUsers").withType("GET"));
                }
                _links.add(linkTo(methodOn(AccountApiController.class).getUser(account.getId(), null)).withRel("getUser").withType("GET"));
                _links.add(linkTo(methodOn(AccountApiController.class).updateUser(account.getId(), null, null, null)).withRel("updateUser").withType("PATCH"));
                _links.add(linkTo(methodOn(AccountApiController.class).mergeUser(account.getId(), null, null, null)).withRel("mergeUser").withType("PUT"));
                _links.add(linkTo(methodOn(AccountApiController.class).deleteUser(account.getId(), null)).withRel("deleteUser").withType("DELETE"));
            }
        }
    }

}
