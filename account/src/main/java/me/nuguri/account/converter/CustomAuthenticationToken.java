package me.nuguri.account.converter;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    UsernamePasswordAuthenticationToken token;

    private String id;

    public CustomAuthenticationToken(UsernamePasswordAuthenticationToken token) {
        super(token.getPrincipal(), token.getCredentials(), token.getAuthorities());
        this.token = token;
    }

    public void setId(Object id) {
        this.id = id.toString();
    }

    @Override
    public boolean isAuthenticated() {
        return token.isAuthenticated();
    }

    @Override
    public Object getDetails() {
        return token.getDetails();
    }
}
