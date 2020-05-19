package me.nuguri.client.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.domain.AccountAdapter;
import me.nuguri.client.entity.Account;
import me.nuguri.client.enums.LoginType;
import me.nuguri.client.properties.LoginProperties;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.enums.Role;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    private final Jackson2JsonParser parser = new Jackson2JsonParser();

    public void nuguriLogin(String code) {
        String access_token = getAccessToken(code, properties.getNuguri().getTokenUrl(), properties.getNuguri().getClientId(),
                properties.getNuguri().getClientSecret(), properties.getNuguri().getRedirectUri());

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(access_token);
        Map<String, Object> infoResultMap = parser.parseMap(restTemplate.exchange(properties.getNuguri().getInfoUrl(),
                HttpMethod.GET, new HttpEntity<>(bearer), String.class).getBody());

        String id = infoResultMap.get("id") + "";
        String name = (String) infoResultMap.get("name");
        setSessionContext(access_token, findOrGenerate(id, name, LoginType.NUGURI));
    }

    public void naverLogin(String code) {
        String access_token = getAccessToken(code, properties.getNaver().getTokenUrl(), properties.getNaver().getClientId(),
                properties.getNaver().getClientSecret(), properties.getNaver().getRedirectUri());

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(access_token);
        Map<String, String> infoResultMap = (Map<String, String>) parser.parseMap(restTemplate.exchange(properties.getNaver().getInfoUrl(),
                HttpMethod.GET, new HttpEntity<>(bearer), String.class).getBody()).get("response");

        String id = infoResultMap.get("id");
        String name = infoResultMap.get("name");
        setSessionContext(access_token, findOrGenerate(id, name, LoginType.NAVER));
    }

    public void facebookLogin(String code) {
        String access_token = getAccessToken(code, properties.getFacebook().getTokenUrl(), properties.getFacebook().getClientId(),
                properties.getFacebook().getClientSecret(), properties.getFacebook().getRedirectUri());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("fields", "id,name");

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(access_token);
        Map<String, Object> infoResultMap = parser.parseMap(restTemplate.exchange(properties.getFacebook().getInfoUrl(),
                HttpMethod.GET, new HttpEntity<>(params, bearer), String.class).getBody());


        String id = (String) infoResultMap.get("id");
        String name = (String) infoResultMap.get("name");
        setSessionContext(access_token, findOrGenerate(id, name, LoginType.FACEBOOK));
    }

    public void googleLogin(String code) {
        String access_token = getAccessToken(code, properties.getGoogle().getTokenUrl(), properties.getGoogle().getClientId(),
                properties.getGoogle().getClientSecret(), properties.getGoogle().getRedirectUri());

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(access_token);
        Map<String, Object> infoResultMap = parser.parseMap(restTemplate.exchange(properties.getGoogle().getInfoUrl(),
                HttpMethod.GET, new HttpEntity<>(bearer), String.class).getBody());

        String id = (String) infoResultMap.get("sub");
        String name = (String) infoResultMap.get("name");
        setSessionContext(access_token, findOrGenerate(id, name, LoginType.GOOGLE));
    }

    public void kakaoLogin(String code) {
        String access_token = getAccessToken(code, properties.getKakao().getTokenUrl(), properties.getKakao().getClientId(),
                properties.getKakao().getClientSecret(), properties.getKakao().getRedirectUri());

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(access_token);
        Map<String, Object> infoResultMap = parser.parseMap(restTemplate.exchange(properties.getKakao().getInfoUrl(),
                HttpMethod.GET, new HttpEntity<>(bearer), String.class).getBody());

        String id = infoResultMap.get("id") + "";
        String name = (String) ((Map) ((Map) infoResultMap.get("kakao_account")).get("profile")).get("nickname");
        setSessionContext(access_token, findOrGenerate(id, name, LoginType.KAKAO));
    }

    /**
     * 외부 Oauth 인증 후 유저 정보가 등록되어 있는지 조회하고 없으면 생성해서 반환
     * @param id 식별키
     * @param name 이름
     * @param loginType Oauth 로그인 타입
     * @return 계정
     */
    private Account findOrGenerate(String id, String name, LoginType loginType) {
        Account account;
        try {
            account = accountService.find(id);
        } catch (UsernameNotFoundException e) {
            account = new Account();
            account.setEmail(id);
            account.setName(name);
            account.setPassword(UUID.randomUUID().toString());
            account.setRoles(new HashSet<>(Arrays.asList(Role.USER)));
            account.setLoginType(loginType);
            account = accountService.generate(account);
        }
        return account;
    }

    /**
     * 외부 OAuth 인증 외부 API 호출 하여 엑세스 토큰 발급 공통 로직
     *
     * @param code         authorization 방식 발급 코드
     * @param tokenUrl     엑세스 토큰 발급 외부 API URL
     * @param clientId     클라이언트 ID
     * @param clientSecret 클라이언트 Secret
     * @param redirectUri  리다이렉트 URL
     * @return 엑세스 토큰
     */
    private String getAccessToken(String code, String tokenUrl, String clientId, String clientSecret, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", GrantType.AUTHORIZATION_CODE.toString());
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpHeaders basic = new HttpHeaders();
        basic.setBasicAuth(clientId, clientSecret);

        Map<String, Object> tokenResultMap = parser.parseMap(restTemplate.postForEntity(tokenUrl, new HttpEntity<>(params, basic), String.class).getBody());
        return (String) tokenResultMap.get("access_token");
    }

    /**
     * 로그인 세션 설정
     *
     * @param access_token 엑세스 토큰
     * @param account 계정
     */
    private void setSessionContext(String access_token, Account account) {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(new AccountAdapter(account), null,
                account.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.toString())).collect(Collectors.toSet())));
        httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        httpSession.setAttribute("access_token", access_token);
    }

}
