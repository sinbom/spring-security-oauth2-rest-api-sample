package me.nuguri.resource.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.resource.common.ResourceServerConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("nuguri");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
                .anyRequest().authenticated();
        http.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

    @Primary
    @Bean
    public RemoteTokenServices remoteTokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(resourceServerConfigProperties.getRemoteServerUrl());
        remoteTokenServices.setClientId(resourceServerConfigProperties.getClientId());
        remoteTokenServices.setClientSecret(resourceServerConfigProperties.getClientSecret());
        return remoteTokenServices;
    }

}
