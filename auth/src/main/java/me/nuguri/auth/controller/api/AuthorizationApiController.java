package me.nuguri.auth.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.auth.annotation.AuthorizationAccessToken;
import me.nuguri.auth.annotation.AuthorizationBearerToken;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.exception.UserNotExistException;
import me.nuguri.auth.service.AccountService;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.enums.Role;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AuthorizationApiController {

    private final TokenStore tokenStore;

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰이 유효하면 만료시킨다.
     * @param token Bearer 토큰
     * @return 토큰 정보
     */
    @PostMapping(value = "/oauth/revoke_token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> revokeToken(@AuthorizationAccessToken DefaultOAuth2AccessToken token) {
        tokenStore.removeAccessToken(token);
        return ResponseEntity.ok(token);
    }

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰으로 토큰을 발급 받은 계정 정보 조회
     * @param token
     * @return
     */
    @GetMapping(value = "/oauth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMe(@AuthorizationAccessToken DefaultOAuth2AccessToken token) {
        try {
            return ResponseEntity.ok(modelMapper.map(accountService.find((Long) token.getAdditionalInformation().get("id")), GetMeResponse.class));
        } catch (UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "not exist account of access token"));
        }
    }

    // ==========================================================================================================================================
    // Domain
    @Getter @Setter
    public static class GetMeResponse {
        private String email;
        private Set<Role> roles;
    }
    // ==========================================================================================================================================
}
