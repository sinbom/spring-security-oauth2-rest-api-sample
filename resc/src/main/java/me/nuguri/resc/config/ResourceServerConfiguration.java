package me.nuguri.resc.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.resc.property.ResourceServerConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@EnableResourceServer
@RequiredArgsConstructor
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private final ResourceServerConfigProperties resourceServerConfigProperties;

    /**
     * 리소스 서버 설정
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("nuguri");
    }

    /**
     * 리소스 서버 필터 체인 설정 모든 url 패턴에 대한 권한 처리
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").access("#oauth2.hasScope('read')")
                .mvcMatchers("/api/**").access("(hasRole('ADMIN') or #oauth2.clientHasRole('ADMIN')) and #oauth2.hasScope('write')")
                .anyRequest().authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.logout().disable();
        http.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

    /**
     * 리소스 서버에서 인증 서버로 check token endpoint 통신할 때 사용
     * @return
     */
    @Bean
    public RemoteTokenServices remoteTokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(resourceServerConfigProperties.getCheckTokenUrl());
        remoteTokenServices.setClientId(resourceServerConfigProperties.getClientId());
        remoteTokenServices.setClientSecret(resourceServerConfigProperties.getClientSecret());
        return remoteTokenServices;
    }

}
