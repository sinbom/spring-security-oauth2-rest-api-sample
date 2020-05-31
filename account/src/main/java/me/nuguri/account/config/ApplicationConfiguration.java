package me.nuguri.account.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.property.AccountServerProperties;
import me.nuguri.common.initializer.EntityInitializer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.StreamUtils;
import sun.misc.IOUtils;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final ResourceLoader resourceLoader;

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        Resource resource = resourceLoader.getResource("classpath:/publicKey.txt");
        String publicKey;
        try {
            InputStream inputStream = resource.getInputStream();
            publicKey = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setVerifierKey(publicKey);
        return jwtAccessTokenConverter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return defaultTokenServices;
    }

    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
    public ApplicationRunner applicationRunner(EntityInitializer entityInitializer, EntityManager em) {
        return (args) -> entityInitializer.init(em);
    }

    /*    *//**
     * 리소스 서버에서 인증 서버로 check token endpoint 통신할 때 사용, 또는 jwt 공개키를 요청할 때 /oauth/token_key 통신시 사용하는 듯
     * @return
     *//*
    @Bean
    public RemoteTokenServices remoteTokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(properties.getCheckTokenUrl());
        // 클라이언트 ID, SECRET을 설정해야 인정 서버의 checkTokenAccess 권한이 isAuthenticated인 경우 토큰 유효성 검사시 헤더를 요청에 함께 전달하여 접근 가능
        remoteTokenServices.setClientId(properties.getClientId());
        remoteTokenServices.setClientSecret(properties.getClientSecret());
        remoteTokenServices.setAccessTokenConverter(new CustomAccessTokenConverter());
        return remoteTokenServices;
    }*/

}
