package me.nuguri.common.initializer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Address;
import me.nuguri.common.entity.Client;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Slf4j
@RequiredArgsConstructor
public class EntityInitializer {

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void init(EntityManager em) {
        log.info("[log] EntityInitializer init entities");
        Account admin = new Account();
        admin.setName("관리자");
        admin.setEmail("admin@naver.com");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setGender(Gender.M);
        admin.setAddress(new Address("경기도 과천시", "부림2길 76 2층", "13830"));
        admin.setRole(Role.ADMIN);

        Account user = new Account();
        user.setName("사용자");
        user.setEmail("user@naver.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setGender(Gender.F);
        user.setAddress(new Address("경기도 과천시", "부림2길 76 2층", "13830"));
        user.setRole(Role.USER);

        em.persist(admin);
        em.persist(user);

        Client allClient = new Client();
        allClient.setClientId("nuguri");
        allClient.setClientSecret(passwordEncoder.encode("bom"));
        allClient.setResourceIds("account,nuguri");
        allClient.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
        allClient.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()));
        allClient.setRedirectUri("http://localhost:9600/main");
        allClient.setAuthorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()));
        allClient.addAccount(admin);

        Client accountClient = new Client();
        accountClient.setClientId("test");
        accountClient.setClientSecret(passwordEncoder.encode("test"));
        accountClient.setResourceIds("account");
        accountClient.setScope(String.join(",", Scope.READ.toString()));
        accountClient.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.CLIENT_CREDENTIALS.toString()));
        accountClient.setRedirectUri("http://localhost:9600/main");
        accountClient.setAuthorities(String.join(",", Role.USER.toString()));
        accountClient.addAccount(user);

        em.persist(allClient);
        em.persist(accountClient);
    }

}
