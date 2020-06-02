package me.nuguri.account.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.account.annotation.TokenAuthenticationUser;
import me.nuguri.account.service.AccountService;
import me.nuguri.account.service.ClientService;
import me.nuguri.common.dto.AccountAdapter;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class ClientApiController {

    private final ClientService clientService;

    private final ClientValidator clientValidator;

    @GetMapping("/api/v1/clients")

//    @GetMapping("/api/v1/client/{id}")
//    @PostMapping("/api/v1/client")
//    @PatchMapping("/api/v1/client/{id}")
//    @PutMapping("/api/v1/client/{id}")
//    @DeleteMapping("/api/v1/client{id}")

    /**
     * 클라이언트 정보 생성
     * @param request resourceIds 접근 리소스, redirectUri 리다이렉트 uri
     * @param errors 에러
     * @param account 현재 인증 토큰 기반 인증 객체
     * @return
     */
    @PostMapping(value = "/api/v1/client", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @PreAuthorize("(hasRole('USER') or #oauth2.clientHasRole('USER')) and #oauth2.hasScope('write')")
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

        public Client toClient(Account account) {
            Client client = new Client();
            client.setClientId(UUID.randomUUID().toString());
            client.setClientSecret(UUID.randomUUID().toString());
            client.setGrantTypes(GrantType.AUTHORIZATION_CODE.toString());
            client.setAuthorities(account.getRole().toString());
            client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
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
    // Validator
    @Component
    public static class ClientValidator {
        public void validate(Client client, Errors errors) {
            if (!Pattern.matches("^(http|https)://.*$", client.getRedirectUri())) {
                errors.rejectValue("redirectUri", "wrong value" ,"redirec uri is must be starts with http or https");
            }
        }
    }
    // ==========================================================================================================================================

}
