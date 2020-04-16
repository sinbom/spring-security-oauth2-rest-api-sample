package me.nuguri.resource.common;

import me.nuguri.resource.config.RestDocsConfiguration;
import me.nuguri.resource.domain.AccessToken;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ResourceServerConfigProperties resourceServerConfigProperties;

    protected String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        headers.setBasicAuth(resourceServerConfigProperties.getClientId(), resourceServerConfigProperties.getClientSecret());
        map.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(resourceServerConfigProperties.getAccessTokenUrl(), HttpMethod.POST, httpEntity, AccessToken.class).getBody().getAccess_token();
    }

    protected String getAccessToken(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        headers.setBasicAuth(resourceServerConfigProperties.getClientId(), resourceServerConfigProperties.getClientSecret());
        map.add("grant_type", "password");
        map.add("username", email);
        map.add("password", password);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(resourceServerConfigProperties.getAccessTokenUrl(), HttpMethod.POST, httpEntity, AccessToken.class).getBody().getAccess_token();
    }

}
