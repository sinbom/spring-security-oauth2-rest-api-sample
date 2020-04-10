package me.nuguri.resource.common;

import lombok.Getter;
import lombok.Setter;
import me.nuguri.resource.entity.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class AccountAdapter extends User {

    private Long id;

    private String email;

    private Set<Role> roles;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), account.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toSet()));
        this.id = account.getId();
        this.email = account.getEmail();
        this.roles = account.getRoles();
    }


}
