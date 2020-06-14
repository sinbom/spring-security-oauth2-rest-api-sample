package me.nuguri.common.adapter;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Getter
public class CustomUserAuthentication extends UsernamePasswordAuthenticationToken {

    private final Long id;

    public CustomUserAuthentication(Authentication authentication, Long id) {
        super(authentication.getPrincipal(), authentication.getCredentials(), authentication.getAuthorities());
        this.id = id;
    }

}
