package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.properties.AuthServerConfigProperties;
import me.nuguri.auth.service.AccountService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    private final TokenStore tokenStore;

    private final DataSource dataSource;

    private final TokenEnhancer tokenEnhancer;

    private final ApprovalStore approvalStore;

    private final AuthenticationManager authenticationManager;

    private final AccountService accountService;

    private final AuthServerConfigProperties authServerConfigProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder)
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    /*
        인메모리 클라이언트 세팅
        clients.inMemory()
                .withClient(authServerConfigProperties.getClientId())
                .secret(passwordEncoder.encode(authServerConfigProperties.getClientSecret()))
                .scopes(Scope.READ.toString(), Scope.WRITE.toString())
                .authorizedGrantTypes(GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                        GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString())
                .redirectUris(authServerConfigProperties.getRedirectUri())
                .accessTokenValiditySeconds(60 * 10)
                .refreshTokenValiditySeconds(60 * 10 * 6);
    */
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore)
                .tokenEnhancer(tokenEnhancer)
                .userDetailsService(accountService)
                .authenticationManager(authenticationManager);
//                .tokenEnhancer(tokenEnhancer); 토근 발급 api 리턴 타입을 구현한 TokenEnhancer 응답으로 사용
    }

}
