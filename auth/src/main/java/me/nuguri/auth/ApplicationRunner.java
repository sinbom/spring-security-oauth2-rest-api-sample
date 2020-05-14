package me.nuguri.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.auth.repository.ClientRepository;
import me.nuguri.auth.service.AccountService;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    private final AuthServerConfigProperties properties;

    @Value("${spring.profiles.active}")
    private String profile;

    @Override
    public void run(ApplicationArguments args) {
        if (profile.equals("local")) {
            log.info("[active profile is " + profile + "] => do persist test entities");

            Account admin = new Account();
            admin.setEmail(properties.getAdminEmail());
            admin.setPassword(properties.getAdminPassword());
            admin.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)));

            Account user = new Account();
            user.setEmail(properties.getUserEmail());
            user.setPassword(properties.getUserPassword());
            user.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

            Client client = new Client();
            client.setClientId(properties.getClientId());
            client.setClientSecret(passwordEncoder.encode(properties.getClientSecret()));
            client.setResourceIds("nuguri");
            client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
            client.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                    GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()));
            client.setRedirectUri(properties.getRedirectUri());
            client.setAuthorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()));
            client.addAccount(admin);

            accountService.generate(admin);
            accountService.generate(user);
        } else {
            log.info("[active profile is " + profile + "] => do not persist test entities");
        }
    }

}
