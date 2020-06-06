package me.nuguri.account.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.common.enums.Role;
import me.nuguri.common.support.EntityInitializer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyUtils;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.StreamUtils;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final ResourceLoader resourceLoader;

    /**
     * 시큐리티 계층형 권한 설정 시큐리티와 리소스 체인 모두 적용
     * @return
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        Map<String, List<String>> roleHierarchyMap = new HashMap<>();
        roleHierarchyMap.put("ROLE_" + Role.ADMIN, Arrays.asList("ROLE_" + Role.USER));
        roleHierarchyMap.put("" + Role.ADMIN, Arrays.asList("" + Role.USER));
        // ROLE_ADIN > ROLE_USER\r\nADMIN > USER 표현식으로 변환해주는 유틸 클래스
        String roleHierarchyExpression = RoleHierarchyUtils.roleHierarchyFromMap(roleHierarchyMap);
        roleHierarchy.setHierarchy(roleHierarchyExpression);
        return roleHierarchy;
    }

    /**
     * JWT 토큰 컨버터 인증 서버의 키로 암호화한 토큰을 복호화 하도록 PK 설정
     * @return
     */
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

    /**
     * 토큰 컨버터를 사용하여 토큰을 복호화하고 인증 객체를 추출하는 토큰 스토어
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 리소스 서버로 token_key 엔드포인트로 PK 얻지 않고 서버내에 PK를 보관해서 복호화 하도록
     * 기본 토큰 서비스 설정(기본으로 설정 되지만 같은 이름으로 2개가 등록되어 test mocking시 autowired가 불가능해서 별도로 설정)
     * @return
     */
    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return defaultTokenServices;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
    public ApplicationRunner applicationRunner(EntityInitializer initializer, EntityManager em) {
        return (args) -> initializer.init(em);
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
