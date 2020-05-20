package me.nuguri.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.account.property.AccountServerProperties;
import me.nuguri.account.service.AccountService;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final AccountService accountService;

    private final AccountServerProperties properties;

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Override
    public void run(ApplicationArguments args) {
        if (profile.equals("local") && ddlAuto.equals("create")) {
            log.info("[log] [active profile is " + profile + "] => do persist test entities");

            Account admin = new Account();
            admin.setName("관리자");
            admin.setEmail(properties.getAdminEmail());
            admin.setPassword(properties.getAdminPassword());
            admin.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)));

            Account user = new Account();
            user.setName("사용자");
            user.setEmail(properties.getUserEmail());
            user.setPassword(properties.getUserPassword());
            user.setRoles(new HashSet<>(Arrays.asList(Role.USER)));

            Client client = new Client();
            client.setClientId(properties.getClientId());
            client.setClientSecret(passwordEncoder.encode(properties.getClientSecret()));
            client.setResourceIds("account,nuguri");
            client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
            client.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                    GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()));
            client.setRedirectUri(properties.getRedirectUri());
            client.setAuthorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()));
            client.addAccount(admin);

            Client client2 = new Client();
            client2.setClientId("test");
            client2.setClientSecret(passwordEncoder.encode("test"));
            client2.setResourceIds("account");
            client2.setScope(String.join(",", Scope.READ.toString()));
            client2.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.CLIENT_CREDENTIALS.toString()));
            client2.setRedirectUri(properties.getRedirectUri());
            client2.setAuthorities(String.join(",", Role.USER.toString()));
            client2.addAccount(user);

            accountService.generate(admin);
            accountService.generate(user);
        } else {
            log.info("[log] [active profile is " + profile + "] => do not persist test entities");
        }
    }

}