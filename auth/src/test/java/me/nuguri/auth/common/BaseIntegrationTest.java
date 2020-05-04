package me.nuguri.auth.common;

import me.nuguri.auth.properties.AuthServerConfigProperties;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Ignore
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    protected AuthServerConfigProperties properties;

    protected void setAdminAuthentication() {
        setAuthentication(properties.getAdminEmail(), properties.getAdminPassword());
    }

    protected void setUserAuthentication() {
        setAuthentication(properties.getUserEmail(), properties.getUserPassword());
    }

    private void setAuthentication(String username, String password) {
        SecurityContextHolder
                .getContext()
                .setAuthentication(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)));
    }

}
