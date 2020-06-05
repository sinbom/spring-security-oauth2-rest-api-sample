package me.nuguri.account.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.account.annotation.TokenAuthenticationUser;
import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.account.repository.ClientRepository;
import me.nuguri.account.service.ClientService;
import me.nuguri.common.dto.BaseResponse;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Scope;
import me.nuguri.common.support.PaginationValidator;
import org.hibernate.EntityMode;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class ClientApiController {

    private final ClientService clientService;

    private final ClientRepository clientRepository;

    private final PaginationValidator paginationValidator;

    private final ClientValidator clientValidator;

    @GetMapping(
            value = "/api/v1/clients",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') and #oauth2.hasScope('read')")
    public ResponseEntity<?> queryClients(PagedResourcesAssembler<Client> assembler, @Valid ClientSearchCondition condition, Errors errors) {
        paginationValidator.validate(condition, Client.class, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, "invalid parameter value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Page<Client> page = clientRepository.pageByConditionFetchAccounts(condition, condition.getPageable());
        if (page.getNumberOfElements() < 1) {
            String message = page.getTotalElements() < 1 ? "content of all pages does not exist" : "content of current page does not exist";
            ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, message);
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
        PagedModel<QueryClientsResource> pagedResources = assembler.toModel(page,
                client -> new QueryClientsResource(new GetClientResponse(client)));
        pagedResources.add(linkTo(ClientApiController.class).slash("/docs/client.html").withRel("document"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping(
            value = "/api/v1/client/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> getClient(@PathVariable Long id) {
        return null;
    }


    @PatchMapping(
            value = "/api/v1/client/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> updateClient(@PathVariable Long id) {
        return null;
    }

    @PutMapping(
            value = "/api/v1/client/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> mergeClient(@PathVariable Long id) {
        return null;
    }

    @DeleteMapping(
            value = "/api/v1/client/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        return null;
    }

    @DeleteMapping(
            value = "/api/v1/clients",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> deleteClients() {
        return null;
    }

    /**
     * 클라이언트 정보 생성
     *
     * @param request resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @param errors  에러
     * @param account 현재 인증 토큰 기반 인증 객체
     * @return
     */
    @PostMapping(
            value = "/api/v1/client",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER') and #oauth2.hasScope('write')")
    public ResponseEntity<?> generateClient(@RequestBody @Valid GenerateClientRequest request, Errors errors, @TokenAuthenticationUser Account account) {
        Client client = request.toClient(account);
        clientValidator.validate(client, errors);
        if (errors.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        String secret = client.getClientSecret();
        Client generate = clientService.generate(client);
        generate.setClientSecret(secret);
        return ResponseEntity.ok(new GetClientResponse(client));
    }

    // ==========================================================================================================================================
    // Domain
    @Getter
    @Setter
    public static class GenerateClientRequest {
        @NotEmpty
        private List<String> resourceIds;
        @NotBlank
        private String redirectUri;

        public Client toClient(Account account) {
            return Client.builder()
                    .clientId(UUID.randomUUID().toString())
                    .clientSecret(UUID.randomUUID().toString())
                    .grantTypes(GrantType.AUTHORIZATION_CODE.toString())
                    .authorities(account.getRole().toString())
                    .scope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()))
                    .redirectUri(redirectUri)
                    .resourceIds(String.join(",", resourceIds))
                    .account(Account.builder().id(account.getId()).build())
                    .build();
        }
    }

    @Getter
    @Setter
    public static class GetClientResponse extends BaseResponse {
        private String clientId;
        private String clientSecret;
        private List<String> resourceIds;
        private List<String> scopes;
        private List<String> grantTypes;
        private List<String> authorities;
        private Integer accessTokenValidity;
        private Integer refreshTokenValidity;
        private String redirectUri;

        public GetClientResponse(Client client) {
            this.clientId = client.getClientId();
            this.clientSecret = client.getClientSecret();
            this.resourceIds = Arrays.asList(client.getResourceIds().split(","));
            this.scopes = Arrays.asList(client.getScope().split(","));
            this.grantTypes = Arrays.asList(client.getGrantTypes().split(","));
            this.authorities = Arrays.asList(client.getAuthorities().split(","));
            this.accessTokenValidity = client.getAccessTokenValidity();
            this.refreshTokenValidity = client.getRefreshTokenValidity();
            this.redirectUri = client.getRedirectUri();
            this.setCreated(client.getCreated());
            this.setUpdated(client.getUpdated());
        }
    }
    // ==========================================================================================================================================
    // Resource
    public static class QueryClientsResource extends EntityModel<GetClientResponse> {
        public QueryClientsResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }
    // ==========================================================================================================================================
    // Validator
    @Component
    public static class ClientValidator {
        public void validate(Client client, Errors errors) {
            if (!Pattern.matches("^(http|https)://.*$", client.getRedirectUri())) {
                errors.rejectValue("redirectUri", "wrong value", "redirec uri is must be starts with http or https");
            }
        }
    }
    // ==========================================================================================================================================

}
