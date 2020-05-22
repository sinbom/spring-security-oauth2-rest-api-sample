package me.nuguri.account.converter;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 기존 컨버터 소스에서 리소스 서버에서 인증 서버로 check point endpoint 통신 시 받아오는 유저 정보 중 ID 값을 포함하는 토큰
 */
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
