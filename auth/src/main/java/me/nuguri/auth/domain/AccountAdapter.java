package me.nuguri.auth.domain;

import me.nuguri.auth.entity.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Collectors;

public class AccountAdapter extends User {

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), account.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet()));
    }

}
