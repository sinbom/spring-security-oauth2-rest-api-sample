package me.nuguri.account.converter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import java.util.Map;

public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

    /**
     * 기존 컨버터 소스에서 리소스 서버에서 인증 서버로 check point endpoint 통신 시 받아오는 유저 정보 중 ID 값을 포함하는 로직 추가
     * @param map
     * @return
     */
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
