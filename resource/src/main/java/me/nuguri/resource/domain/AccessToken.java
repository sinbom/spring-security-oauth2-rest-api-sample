package me.nuguri.resource.domain;

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

    private String scope;

    private String expires_in;

    private Date expiration;

}
