package me.nuguri.client.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.entity.Account;
import me.nuguri.client.properties.LoginProperties;
import me.nuguri.client.repository.AccountRepository;
import me.nuguri.common.enums.Role;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginProperties properties;

    private final RestTemplate restTemplate;

    private final AccountService accountService;

    private final HttpSession httpSession;

    public void nuguriLogin(String code) {
        Jackson2JsonParser parser = new Jackson2JsonParser();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", properties.getNuguri().getRedirectUri());

        HttpHeaders basic = new HttpHeaders();
        basic.setBasicAuth(properties.getNuguri().getClientId(), properties.getNuguri().getClientSecret());

        Map<String, Object> tokenResultMap = parser.parseMap(restTemplate.postForEntity(properties.getNuguri().getTokenUrl(),
                new HttpEntity<>(params, basic), String.class).getBody());

        Account account;
        String id = (tokenResultMap.get("id")) + "";
        try {
            account = accountService.find(id);
        } catch (UsernameNotFoundException e) {
            HttpHeaders bearer = new HttpHeaders();
            bearer.setBearerAuth((String) tokenResultMap.get("access_token"));
            Map<String, Object> infoResultMap = parser.parseMap(restTemplate.exchange(properties.getNuguri().getInfoUrl(),
                    HttpMethod.GET, new HttpEntity<>(bearer), String.class).getBody());
            account = new Account();
            account.setEmail(id);
            account.setName((String) infoResultMap.get("name"));
            account.setPassword(UUID.randomUUID().toString());
            account.setRoles(new HashSet<>(Arrays.asList(Role.USER)));
            account = accountService.generate(account);
        }

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(account.getId(), null,
                account.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toSet())));
        httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

}
