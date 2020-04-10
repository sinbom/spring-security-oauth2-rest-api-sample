package me.nuguri.auth;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.common.Role;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.repository.AccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthServerConfigProperties authServerConfigProperties;

    @Override
    public void run(ApplicationArguments args) {
        accountRepository.save(Account.builder()
                .email(authServerConfigProperties.getAdminEmail())
                .password(passwordEncoder.encode(authServerConfigProperties.getAdminPassword()))
                .roles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)))
                .build());
    }

}
