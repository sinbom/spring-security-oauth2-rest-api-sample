package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.property.AuthServerConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
@EnableResourceServer
public class AuthorizationServerConfiguration {

    @Configuration
    @RequiredArgsConstructor
    public static class AuthorizationConfiguration extends AuthorizationServerConfigurerAdapter {

        private final PasswordEncoder passwordEncoder;

        private final TokenStore tokenStore;

        private final DataSource dataSource;

        private final TokenEnhancer tokenEnhancer;

        private final AuthenticationManager authenticationManager;

        private final UserDetailsService userDetailsService;

        private final AuthServerConfigProperties properties;

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                    .passwordEncoder(passwordEncoder)
                    .tokenKeyAccess("permitAll()")
                    .checkTokenAccess("permitAll()");
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.jdbc(dataSource);
/*        인메모리 클라이언트 세팅
        clients.inMemory()
                .withClient(properties.getClientId())
                .secret(passwordEncoder.encode(properties.getClientSecret()))
                .scopes(Scope.READ.toString(), Scope.WRITE.toString())
                .authorizedGrantTypes(GrantType.PASSWORD.toString(), GrantType.AUTHORIZATION_CODE.toString(),
                        GrantType.IMPLICIT.toString(), GrantType.CLIENT_CREDENTIALS.toString(), GrantType.REFRESH_TOKEN.toString())
                .redirectUris(properties.getRedirectUri())
                .accessTokenValiditySeconds(60 * 10)
                .refreshTokenValiditySeconds(60 * 10 * 6);*/
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .tokenStore(tokenStore)
                    .tokenEnhancer(tokenEnhancer)
                    .userDetailsService(userDetailsService)
                    .authenticationManager(authenticationManager);
        }

    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @Order(100)
    @RequiredArgsConstructor
    public static class ResourceConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("account");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .mvcMatchers("/api/**")
                    .and()
                    .authorizeRequests()
                    .mvcMatchers(HttpMethod.POST, "/api/**/user").permitAll()
                    .anyRequest().authenticated();
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.logout().disable();
            http.csrf().disable();
            http.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
        }

    }

}
