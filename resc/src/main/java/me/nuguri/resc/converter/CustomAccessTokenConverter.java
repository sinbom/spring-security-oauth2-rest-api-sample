package me.nuguri.resc.converter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.*;

public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

    private UserAuthenticationConverter userTokenConverter = new DefaultUserAuthenticationConverter();

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
        Authentication authentication = oAuth2Authentication.getUserAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            oAuth2Authentication.getOAuth2Request();
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
            usernamePasswordAuthenticationToken.getPrincipal();
            usernamePasswordAuthenticationToken.getCredentials();
            usernamePasswordAuthenticationToken.getAuthorities();
            usernamePasswordAuthenticationToken.getDetails();
            usernamePasswordAuthenticationToken.getAuthorities();


            return new UsernamePasswordAuthenticationToken() {

            };
        } else {
            return oAuth2Authentication;
        }

        Map<String, String> parameters = new HashMap<>();
        Set<String> scope = extractScope(map);
        Authentication user = this.userTokenConverter.extractAuthentication(map);
        String clientId = (String)map.get(this.clientIdAttribute);
        parameters.put(this.clientIdAttribute, clientId);
        if (this.includeGrantType && map.containsKey("grant_type")) {
            parameters.put("grant_type", (String)map.get("grant_type"));
        }

        Set<String> resourceIds = new LinkedHashSet((Collection)(map.containsKey("aud") ? this.getAudience(map) : Collections.emptySet()));
        Collection<? extends GrantedAuthority> authorities = null;
        if (user == null && map.containsKey("authorities")) {
            String[] roles = (String[])((Collection)map.get("authorities")).toArray(new String[0]);
            authorities = AuthorityUtils.createAuthorityList(roles);
        }

        OAuth2Request request = new OAuth2Request(parameters, clientId, authorities, true, scope, resourceIds, (String)null, (Set)null, (Map)null);
        return new OAuth2Authentication(request, user);
    }
}
