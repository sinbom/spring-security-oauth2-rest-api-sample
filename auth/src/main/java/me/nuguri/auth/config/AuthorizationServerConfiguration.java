package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.common.AuthServerConfigProperties;
import me.nuguri.auth.common.GrantType;
import me.nuguri.auth.common.Scope;
import me.nuguri.auth.service.AccountService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    private final TokenStore tokenStore;

    private final AuthenticationManager authenticationManager;

    private final AccountService accountService;

    private final AuthServerConfigProperties authServerConfigProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder)
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(authServerConfigProperties.getClientId())
                .secret(passwordEncoder.encode(authServerConfigProperties.getClientSecret()))
                .scopes(Scope.READ.toString(), Scope.WRITE.toString())
                .authorizedGrantTypes(GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                        GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString())
                .redirectUris(authServerConfigProperties.getRedirectUri())
                .accessTokenValiditySeconds(60 * 10)
                .refreshTokenValiditySeconds(60 * 10 * 6);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore)
                .userDetailsService(accountService)
                .authenticationManager(authenticationManager);
    }

}
