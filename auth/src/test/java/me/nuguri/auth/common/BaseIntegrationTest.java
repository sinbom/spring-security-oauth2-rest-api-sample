package me.nuguri.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.auth.property.AuthServerConfigProperties;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    private AuthenticationManager authenticationManager;

    @Autowired
    protected AuthServerConfigProperties properties;

    @Autowired
    protected ObjectMapper objectMapper;

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
