package me.nuguri.auth.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class AccessToken {

    private String access_token;

    private String refresh_token;

    private String token_type;

    private Set<String> scope;

    private int expires_in;

    private Date expiration;

    public AccessToken(OAuth2AccessToken oAuth2AccessToken) {
        this.access_token = oAuth2AccessToken.getValue();
        this.refresh_token = oAuth2AccessToken.getRefreshToken().getValue();
        this.token_type = oAuth2AccessToken.getTokenType();
        this.scope = oAuth2AccessToken.getScope();
        this.expires_in = oAuth2AccessToken.getExpiresIn();
        this.expiration = oAuth2AccessToken.getExpiration();
    }

}
