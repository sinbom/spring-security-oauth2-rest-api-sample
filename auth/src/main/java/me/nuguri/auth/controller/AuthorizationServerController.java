package me.nuguri.auth.controller;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.domain.AccessToken;
import me.nuguri.auth.domain.AccessTokenResource;
import me.nuguri.auth.domain.AccountAdapter;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class AuthorizationServerController {

    private final TokenStore tokenStore;

    @GetMapping(value = {"/main", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String main() {
        return "main";
    }


    @PostMapping(value = "/oauth/revoke_token", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> revokeToken(@RequestHeader(required = false) String authorization) {
        if (StringUtils.isEmpty(authorization)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        OAuth2AccessToken access_token = tokenStore.readAccessToken(authorization.replace("Bearer", "").trim());
        if (access_token == null) {
            return ResponseEntity.badRequest().build();
        }
        tokenStore.removeAccessToken(access_token);
        return ResponseEntity.ok(new AccessTokenResource(new AccessToken(access_token)));
    }

}
