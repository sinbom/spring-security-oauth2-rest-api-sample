package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.common.initializer.EntityInitializer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyStore;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final ResourceLoader resourceLoader;

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        Resource resource = resourceLoader.getResource("classpath:/oauth2jwt.jks");
        char[] password = "oauth2jwt".toCharArray();
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, password);
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("oauth2jwt");
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setKeyPair(keyPair);
        return jwtAccessTokenConverter;
    }

    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
    public ApplicationRunner applicationRunner(EntityInitializer entityInitializer, EntityManager em) {
        return (args) -> entityInitializer.init(em);
    }

    /**
     * 인증 서버 엔드 포인트 /oauth/token, /oauth/check_token, /oauth/revoke_token
     * 응답 JSON 값에 추가 정보인 계정 식별키 id 값을 포함해서 응답
     *
     * @return
     */
/*    @Bean
    public TokenEnhancer tokenEnhancer() {                // jwt아닌 토큰 발급 방식에서 토큰 생성 시 유저 식별키 포함해서 반환하도록 응답 객체를 만드는 tokenEnhancer
        return (oAuth2AccessToken, oAuth2Authentication) -> {
            String grantType = oAuth2Authentication.getOAuth2Request().getGrantType();
            if (!grantType.equals(GrantType.CLIENT_CREDENTIALS.toString())) {
                AccountAdapter account = (AccountAdapter) oAuth2Authentication.getPrincipal();
                Map<String, Object> info = new HashMap<>();
                info.put("id", account.getAccount().getId());
                ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(info);
            }
            return oAuth2AccessToken;
        };
    }*/

 /*   @Bean            // jdbc 방식 토큰 스토어
    public TokenStore tokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }*/

}
