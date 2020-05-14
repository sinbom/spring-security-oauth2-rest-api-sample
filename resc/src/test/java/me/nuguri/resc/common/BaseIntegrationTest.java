package me.nuguri.resc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.resc.config.RestDocsConfiguration;
import me.nuguri.resc.property.ResourceServerConfigProperties;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Disabled
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ResourceServerConfigProperties resourceServerConfigProperties;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String getAccessToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return requestAccessToken(map);
    }

    protected String getAccessToken(String email, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", email);
        map.add("password", password);
        return requestAccessToken(map);
    }

    private String requestAccessToken(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(resourceServerConfigProperties.getClientId(), resourceServerConfigProperties.getClientSecret());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        return (String) new JacksonJsonParser()
                .parseMap(restTemplate.exchange(resourceServerConfigProperties.getAccessTokenUrl(), HttpMethod.POST, httpEntity, String.class).getBody())
                .get("access_token");
    }

}
