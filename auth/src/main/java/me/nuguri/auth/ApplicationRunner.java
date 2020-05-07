package me.nuguri.auth;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.auth.enums.GrantType;
import me.nuguri.auth.enums.Role;
import me.nuguri.auth.enums.Scope;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.repository.AccountRepository;
import me.nuguri.auth.repository.ClientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final AccountRepository accountRepository;

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthServerConfigProperties authServerConfigProperties;

    @Override
    public void run(ApplicationArguments args) {
        Account admin = accountRepository.save(Account.builder()
                .email(authServerConfigProperties.getAdminEmail())
                .password(passwordEncoder.encode(authServerConfigProperties.getAdminPassword()))
                .roles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)))
                .build());

        accountRepository.save(Account.builder()
                .email(authServerConfigProperties.getUserEmail())
                .password(passwordEncoder.encode(authServerConfigProperties.getUserPassword()))
                .roles(new HashSet<>(Arrays.asList(Role.USER)))
                .build());

        clientRepository.save(Client.builder()
                .clientId(authServerConfigProperties.getClientId())
                .resourceIds("nuguri")
                .clientSecret(passwordEncoder.encode(authServerConfigProperties.getClientSecret()))
                .scope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()))
                .grantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                        GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()))
                .redirectUri(authServerConfigProperties.getRedirectUri())
                .authorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()))
                .accessTokenValidity(600)
                .refreshTokenValidity(3600)
                .autoapprove(null)
                .account(admin)
                .build());

    }

}
