package me.nuguri.account.converter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import java.util.Map;

public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
        Authentication authentication = oAuth2Authentication.getUserAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CustomAuthenticationToken customAuthentication = new CustomAuthenticationToken((UsernamePasswordAuthenticationToken) authentication);
            customAuthentication.setId(map.get("id"));
            return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), customAuthentication);
        } else {
            return oAuth2Authentication;
        }
    }
}
