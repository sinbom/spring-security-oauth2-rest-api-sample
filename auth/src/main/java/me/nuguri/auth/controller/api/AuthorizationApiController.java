package me.nuguri.auth.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.nuguri.auth.annotation.AuthorizationBearerToken;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> revokeToken(@AuthorizationBearerToken String token) {
        if (StringUtils.isEmpty(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
        if (oAuth2AccessToken == null) {
            return ResponseEntity.badRequest().build();
        }
        tokenStore.removeAccessToken(oAuth2AccessToken);
        return ResponseEntity.ok(oAuth2AccessToken);
    }

    /**
     * Authorization Header 통해 전달 받은 Bearer 토큰으로 계정 정보를 조회
     * @param token
     * @return
     */
    @GetMapping(value = "/oauth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMe(@AuthorizationBearerToken String token) {
        if (StringUtils.isEmpty(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
        if (oAuth2AccessToken == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(modelMapper.map(accountService.find((Long) oAuth2AccessToken.getAdditionalInformation().get("id")), GetMeResponse.class));
    }

    @Data
    public static class GetMeResponse {
        private String email;
        private Set<Role> roles;
    }

}
