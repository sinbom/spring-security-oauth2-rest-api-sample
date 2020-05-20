package me.nuguri.common.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.entity.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode(of = "account", callSuper = false)
public class AccountAdapter extends User implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), account.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet()));
        this.account = account;
    }

}
