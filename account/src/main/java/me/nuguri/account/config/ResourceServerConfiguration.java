package me.nuguri.account.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.converter.CustomAccessTokenConverter;
import me.nuguri.account.property.AccountServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@EnableAuthorizationServer
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true) // 애노테이션 기반 권한 검사 사용
@RequiredArgsConstructor
@Order(100) // 시큐리티 필터 체인보다 우선순위를 낮게 하여 우선적으로 시큐리티 필터 체인의 url 패턴으로 검사
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private final AccountServerProperties properties;

    /**
     * 리소스 서버 설정
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("account");
    }

    /**
     * 리소스 서버 필터 체인 설정, /api/** url 패턴에 대한 권한 처리
     * @param http
     * @throws Exception
     */
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

    /**
     * 리소스 서버에서 인증 서버로 check token endpoint 통신할 때 사용
     * @return
     */
    @Bean
    public RemoteTokenServices remoteTokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(properties.getCheckTokenUrl());
        // 클라이언트 ID, SECRET을 설정해야 인정 서버의 checkTokenAccess 권한이 isAuthenticated인 경우 토큰 유효성 검사시 헤더를 요청에 함께 전달하여 접근 가능
        remoteTokenServices.setClientId(properties.getClientId());
        remoteTokenServices.setClientSecret(properties.getClientSecret());
        remoteTokenServices.setAccessTokenConverter(new CustomAccessTokenConverter());
        return remoteTokenServices;
    }

}
