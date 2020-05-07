package me.nuguri.auth.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.auth.annotation.AuthenticationUser;
import me.nuguri.auth.domain.ErrorResponse;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.enums.GrantType;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.enums.Scope;
import me.nuguri.auth.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ClientApiController {

    private final ClientService clientService;

    @PostMapping(value = "/api/v1/client", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateClient(@RequestBody @Valid GenerateClientRequest request, Errors errors, @AuthenticationUser Account loginAccount) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors));
        }
        Client client = request.toClient(loginAccount);
        String clientSecret = client.getClientSecret();
        client = clientService.generate(client);
        client.setClientSecret(clientSecret);
        return ResponseEntity.ok(new GenerateClientResponse(client));
    }

    @Data
    public static class GenerateClientRequest {
        @NotEmpty
        private List<String> resourceIds;
        @NotBlank

        private String redirectUri;
        private Client toClient(Account account) {
            return Client.builder()
                    .clientId(UUID.randomUUID().toString())
                    .clientSecret(UUID.randomUUID().toString())
                    .grantTypes(String.join(",", GrantType.AUTHORIZATION_CODE.toString(), GrantType.IMPLICIT.toString()))
                    .authorities(account.getRoles().stream().map(Role::toString).collect(Collectors.joining(",")))
                    .scope(account.getRoles().stream().anyMatch(Role.ADMIN::equals) ? Scope.READ + "," + Scope.WRITE : Scope.READ.toString())
                    .accessTokenValidity(600)
                    .refreshTokenValidity(3600)
                    .redirectUri(redirectUri)
                    .resourceIds(String.join(",", resourceIds))
                    .account(account)
                    .build();
        }
    }

    @Data
    public static class GenerateClientResponse {
        private String clientId;
        private String clientSecret;
        private List<String> resourceIds;
        private List<String> scopes;
        private List<String> grantTypes;
        private List<String> authorities;
        private Integer accessTokenValidity;
        private Integer refreshTokenValidity;
        private String redirectUri;
        public GenerateClientResponse(Client client) {
            this.clientId = client.getClientId();
            this.clientSecret = client.getClientSecret();
            this.resourceIds = Arrays.asList(client.getResourceIds().split(","));
            this.scopes = Arrays.asList(client.getScope().split(","));
            this.grantTypes = Arrays.asList(client.getGrantTypes().split(","));
            this.authorities = Arrays.asList(client.getAuthorities().split(","));
            this.accessTokenValidity = client.getAccessTokenValidity();
            this.refreshTokenValidity = client.getRefreshTokenValidity();
            this.redirectUri = client.getRedirectUri();
        }
    }

}
