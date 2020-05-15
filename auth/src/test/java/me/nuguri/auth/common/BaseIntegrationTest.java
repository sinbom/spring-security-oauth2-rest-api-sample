package me.nuguri.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.entity.Client;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.auth.service.AccountService;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import me.nuguri.common.enums.Scope;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Disabled
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthServerConfigProperties properties;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AccountService accountService;

    protected void generateTestEntities() {
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
        client.setResourceIds("account");
        client.setScope(String.join(",", Scope.READ.toString(), Scope.WRITE.toString()));
        client.setGrantTypes(String.join(",", GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString()));
        client.setRedirectUri(properties.getRedirectUri());
        client.setAuthorities(String.join(",", Role.ADMIN.toString(), Role.USER.toString()));
        client.addAccount(admin);

        accountService.generate(admin);
        accountService.generate(user);
    }

}
