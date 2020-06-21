package me.nuguri.common.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class AuthenticationAdapter {

    private final CustomUserAuthentication authentication;

    public Long getId() {
        return authentication.getId();
    }

    public List<String> getAuthorities() {
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(toList());
    }

}
