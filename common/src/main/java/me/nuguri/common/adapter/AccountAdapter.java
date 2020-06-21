package me.nuguri.common.adapter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.nuguri.common.entity.Account;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.Collections;

@Getter
@Setter
@EqualsAndHashCode(of = "account", callSuper = false)
public class AccountAdapter extends User implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Account account;

    public AccountAdapter(Account account) {
        super(
                account.getEmail(),
                account.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_" + account.getAuthority().getName())
        );
        this.account = account;
    }

}
