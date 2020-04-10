package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(account.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet()))
                .build();
    }

    public Account join(Account account) {
        return accountRepository.save(account);
    }

    public void withdrawal(Account account) {
        accountRepository.delete(account);
    }

}
