package me.nuguri.auth.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.auth.service.ClientService;
import me.nuguri.common.domain.AccountAdapter;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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

@RestController
@RequiredArgsConstructor
public class ClientApiController {

    private final ClientService clientService;

    /**
     * 클라이언트 정보 생성
     * @param request resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @param errors 에러
     * @param authentication 현재 인증 토큰 기반 인증 객체
     * @return
     */
    @PostMapping(value = "/api/v1/client", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> generateClient(@RequestBody @Valid GenerateClientRequest request, Errors errors, OAuth2Authentication authentication) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, "invalid value", errors));
        }
        Client client = request.toClient(((AccountAdapter) authentication.getPrincipal()).getAccount());
        String clientSecret = client.getClientSecret();
        client = clientService.generate(client);
        client.setClientSecret(clientSecret);
        return ResponseEntity.ok(new GenerateClientResponse(client));
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

        private Client toClient(Account account) {
            Client client = new Client();
            client.setClientId(UUID.randomUUID().toString());
            client.setClientSecret(UUID.randomUUID().toString());
            client.setGrantTypes(GrantType.AUTHORIZATION_CODE.toString());
            client.setAuthorities(Role.USER.toString());
            client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
            client.setAccessTokenValidity(600);
            client.setRefreshTokenValidity(3600);
            client.setRedirectUri(redirectUri);
            client.setResourceIds(String.join(",", resourceIds));
            client.setAccount(account);
            return client;
        }
    }

    @Getter
    @Setter
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
    // ==========================================================================================================================================

}
