package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.repository.AccountRepository;
import me.nuguri.auth.repository.ClientRepository;
import me.nuguri.auth.service.lazy.AuthorizationLazyService;
import me.nuguri.common.adapter.AccountAdapter;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Client;
import me.nuguri.common.entity.ClientRedirectUri;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizationService implements UserDetailsService, ClientDetailsService {

    private final AccountRepository accountRepository;

    private final ClientRepository clientRepository;

    private final AuthorizationLazyService authorizationLazyService;

    /**
     * 시큐리티 로그인 및 인증 토큰 발급(password 방식) 수행 시 사용, 유저 엔티티 대리키(email) 조회
     *
     * @param email 이메일
     * @return 유저 엔티티 래핑 + 시큐리티 인증 객체
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new AccountAdapter(account);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = authorizationLazyService.findByClientIdFetchAndLazy(clientId);
        // 클라이언트 시크릿
        String clientSecret = client.getClientSecret();
        // 인증 토큰 유효 시간
        Integer accessTokenValidity = client.getAccessTokenValidity();
        // 재발급 토큰 유효 시간
        Integer refreshTokenValidity = client.getRefreshTokenValidity();
        // 접근 가능 리소스 서버
        List<String> resourceIds = client
                .getClientResources()
                .stream()
                .map(c -> c.getResource().getName())
                .collect(toList());
        // 인증 부여 방식
        List<String> grantTypes = client
                .getClientGrantTypes()
                .stream()
                .map(c -> c.getGrantType().toString())
                .collect(toList());
        // 접근 가능 범위
        List<String> scopes = client
                .getClientScopes()
                .stream()
                .map(c -> c.getScope().getName())
                .collect(toList());
        // 접근 가능 권한
        List<GrantedAuthority> authorities = client
                .getClientAuthorities()
                .stream()
                .map(c -> new SimpleGrantedAuthority("ROLE_" + c.getAuthority().getName()))
                .collect(toList());
        // 리다이렉트 경로
        Set<String> redirectUris = client
                .getClientRedirectUris()
                .stream()
                .map(ClientRedirectUri::getUri)
                .collect(toSet());
        // 클라이언트 정보 어댑터 객체 생성
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(clientId);
        clientDetails.setClientSecret(clientSecret);
        clientDetails.setResourceIds(resourceIds);
        clientDetails.setAccessTokenValiditySeconds(accessTokenValidity);
        clientDetails.setRefreshTokenValiditySeconds(refreshTokenValidity);
        clientDetails.setRegisteredRedirectUri(redirectUris);
        clientDetails.setAuthorizedGrantTypes(grantTypes);
        clientDetails.setScope(scopes);
        clientDetails.setAuthorities(authorities);
        return clientDetails;
    }
}
