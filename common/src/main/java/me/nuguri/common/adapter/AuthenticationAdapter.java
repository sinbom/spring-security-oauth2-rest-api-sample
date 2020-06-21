package me.nuguri.common.adapter;

import lombok.RequiredArgsConstructor;
import me.nuguri.common.enums.Roles;
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

    public List<Roles> getAuthorities() {
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities
                .stream()
                .map(a -> {
                    String role = a.getAuthority().replace("ROLE_", "");
                    return Roles.valueOf(role);
                })
                .collect(toList());
    }

}
