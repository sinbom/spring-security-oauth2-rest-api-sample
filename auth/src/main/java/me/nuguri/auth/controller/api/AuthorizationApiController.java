package me.nuguri.auth.controller.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nuguri.auth.annotation.Oauth2AccessToken;
import me.nuguri.auth.annotation.Oauth2Authentication;
import me.nuguri.auth.domain.AccountAdapter;
import me.nuguri.auth.entity.Account;
import me.nuguri.common.enums.Role;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AuthorizationApiController {

    private final TokenStore tokenStore;

    private final ModelMapper modelMapper;

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰이 유효하면 만료시킨다.
     * @param accessToken 엑세스 토큰
     * @return
     */
    @PostMapping(value = "/oauth/revoke_token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> revokeToken(@Oauth2AccessToken DefaultOAuth2AccessToken accessToken) {
        tokenStore.removeAccessToken(accessToken);
        return ResponseEntity.ok(accessToken);
    }

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰으로 토큰을 발급 받은 계정 정보 조회
     * @param authentication 인증 객체
     * @return
     */
    @GetMapping(value = "/oauth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMe(@Oauth2Authentication OAuth2Authentication authentication) {
        Account account = ((AccountAdapter) authentication.getPrincipal()).getAccount();
        return ResponseEntity.ok(modelMapper.map(account, GetMeResponse.class));
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
