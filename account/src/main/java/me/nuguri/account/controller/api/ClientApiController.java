package me.nuguri.account.controller.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.account.annotation.HasAuthority;
import me.nuguri.account.annotation.TokenAuthentication;
import me.nuguri.account.annotation.TokenAuthenticationUser;
import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.account.repository.ClientRepository;
import me.nuguri.account.service.ClientService;
import me.nuguri.account.service.lazy.ClientLazyService;
import me.nuguri.common.adapter.AuthenticationAdapter;
import me.nuguri.common.dto.BaseResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import me.nuguri.common.support.BaseValidator;
import me.nuguri.common.support.PaginationValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class ClientApiController {

    private final ClientService clientService;

    private final ClientLazyService clientLazyService;

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
        Pageable pageable = condition.getPageable();
        Page<Client> page = clientRepository.pageByConditionFetchAccounts(condition, pageable);
        paginationValidator.checkEmpty(page);
        PagedModel<QueryClientsResource> pagedResources = assembler.toModel(page,
                client -> new QueryClientsResource(new GetClientResponse(client)));
        pagedResources.add(linkTo(ClientApiController.class).slash("/docs/client.html").withRel("document"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping(
            value = "/api/v1/client/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER') and #oauth2.hasScope('read')")
    @HasAuthority
    public ResponseEntity<?> getClient(@PathVariable Long id, @TokenAuthentication AuthenticationAdapter authentication) {
        Long ownerId = authentication.getId();
        Client client = clientService.findById(id, authentication);
        GetClientResponse getClientResponse = new GetClientResponse(client);
        GetClientResource getClientResource = new GetClientResource(getClientResponse);
        return ResponseEntity.ok(getClientResource);
    }

    /**
     * 클라이언트 정보 생성
     *
     * @param request resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @param errors  에러
     * @param user    현재 인증 토큰 기반 인증 객체
     * @return
     */
    @PostMapping(
            value = "/api/v1/client",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER') and #oauth2.hasScope('write')")
    public ResponseEntity<?> generateClient(@RequestBody @Valid GenerateClientRequest request, Errors errors, @TokenAuthenticationUser Account user) {
        Client client = request.toClient(user);
        clientValidator.validate(client, errors);
        Client generate = clientService.generate(client);
        GetClientResponse getClientResponse = new GetClientResponse(generate);
        GenerateClientResource generateClientResource = new GenerateClientResource(getClientResponse);
        return ResponseEntity
                .created(linkTo(methodOn(ClientApiController.class).generateClient(null, null, null)).toUri())
                .body(generateClientResource);
    }


    @PatchMapping(
            value = "/api/v1/client/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER') and #oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody UpdateClientRequest request, Errors errors,
                                          @TokenAuthentication AuthenticationAdapter authentication) {
/*        Client client = request.toClient(id);
        clientValidator.validate(client, errors);
        Client update = clientLazyService.update(client, authentication);
        GetClientResponse getClientResponse = new GetClientResponse(update);
        UpdateClientResource updateClientResource = new UpdateClientResource(getClientResponse);
        return ResponseEntity.ok(updateClientResource);*/
        return null;
    }

    @PutMapping(
            value = "/api/v1/client/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')and #oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> mergeClient(@PathVariable Long id, @RequestBody @Valid UpdateClientRequest request, Errors errors, @TokenAuthenticationUser Account user) {
        Client client = request.toClient(id);
        clientValidator.validate(client, errors);
        Client merge = clientService.merge(client);
        GetClientResponse getClientResponse = new GetClientResponse(merge);
        MergeClientResource mergeClientResource = new MergeClientResource(getClientResponse);
        return ResponseEntity.ok(mergeClientResource);
    }

    @DeleteMapping(
            value = "/api/v1/client/{id}",
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')and #oauth2.hasScope('write')")
    @HasAuthority
    public ResponseEntity<?> deleteClient(@PathVariable Long id, @TokenAuthenticationUser(entityGraph = "clients") Account user) {
        clientRepository.deleteById(id);
        DeleteClientResposne deleteClientResposne = new DeleteClientResposne(1);
        DeleteClientResource deleteClientResource = new DeleteClientResource(deleteClientResposne, id);
        return ResponseEntity.ok(deleteClientResource);

    }

    @DeleteMapping(
            value = "/api/v1/clients",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') and #oauth2.hasScope('write')")
    public ResponseEntity<?> deleteClients(@RequestBody @Valid DeleteClientsRequest request) {
        List<Long> ids = request.getIds();
        long count = clientRepository.deleteByIdsBatchInQuery(ids);
        DeleteClientResposne deleteClientResposne = new DeleteClientResposne(count);
        DeleteClientsResource deleteClientsResource = new DeleteClientsResource(deleteClientResposne);
        return ResponseEntity.ok(deleteClientsResource);
    }

    // ==========================================================================================================================================
    // DTO
    @Getter
    @Setter
    public static class GenerateClientRequest {
        @NotEmpty
        private List<String> resourceIds;
        @Pattern(regexp = "^(http|https)://.+$")
        private String redirectUri;

        public Client toClient(Account account) {
            return Client.builder()
                    .clientId(UUID.randomUUID().toString())
                    .clientSecret(UUID.randomUUID().toString())
                    .grantTypes(GrantType.AUTHORIZATION_CODE.toString())
                    .authority(account.getRole())
                    .scope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()))
                    .redirectUri(redirectUri)
                    .resourceIds(String.join(",", resourceIds))
                    .account(
                            Account.builder()
                                    .id(account.getId())
                                    .build()
                    )
                    .build();
        }
    }

    @Getter
    @Setter
    public static class UpdateClientRequest {
        @NotEmpty
        private List<String> resourceIds;
        @Pattern(regexp = "^(http|https)://.+$")
        private String redirectUri;

        public Client toClient(Long id) {
            return Client.builder()
                    .id(id)
                    .redirectUri(redirectUri)
                    .resourceIds(String.join(",", resourceIds))
                    .build();
        }
    }

    @Getter
    @Setter
    private static class DeleteClientsRequest {
        @NotEmpty
        private List<Long> ids;
    }

    @Getter
    @Setter
    public static class GetClientResponse extends BaseResponse {
        private String clientId;
        private String clientSecret;
        private List<String> resourceIds;
        private List<String> scopes;
        private List<String> grantTypes;
        private Role authority;
        private Integer accessTokenValidity;
        private Integer refreshTokenValidity;
        private String redirectUri;

        public GetClientResponse(Client client) {
            this.clientId = client.getClientId();
            this.clientSecret = client.getClientSecret();
            this.resourceIds = Arrays.asList(client.getResourceIds().split(","));
            this.scopes = Arrays.asList(client.getScope().split(","));
            this.grantTypes = Arrays.asList(client.getGrantTypes().split(","));
            this.authority = client.getAuthority();
            this.accessTokenValidity = client.getAccessTokenValidity();
            this.refreshTokenValidity = client.getRefreshTokenValidity();
            this.redirectUri = client.getRedirectUri();
            this.setCreated(client.getCreated());
            this.setUpdated(client.getUpdated());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DeleteClientResposne {
        private long count;
    }

    // ==========================================================================================================================================
    // Resource
    public static class QueryClientsResource extends EntityModel<GetClientResponse> {
        public QueryClientsResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }

    public class GetClientResource extends EntityModel<GetClientResponse> {
        public GetClientResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }

    public class GenerateClientResource extends EntityModel<GetClientResponse> {
        public GenerateClientResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }

    public class UpdateClientResource extends EntityModel<GetClientResponse> {
        public UpdateClientResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }

    public class MergeClientResource extends EntityModel<GetClientResponse> {
        public MergeClientResource(GetClientResponse content, Link... links) {
            super(content, links);
        }
    }

    public class DeleteClientResource extends EntityModel<DeleteClientResposne> {
        public DeleteClientResource(DeleteClientResposne content, Long id, Link... links) {
            super(content, links);
        }
    }

    public class DeleteClientsResource extends EntityModel<DeleteClientResposne> {
        public DeleteClientsResource(DeleteClientResposne content, Link... links) {
            super(content, links);
        }
    }

    // ==========================================================================================================================================
    // Validator
    @Component
    public static class ClientValidator extends BaseValidator {
        public void validate(Client client, Errors errors) {
        }
    }
    // ==========================================================================================================================================

}
