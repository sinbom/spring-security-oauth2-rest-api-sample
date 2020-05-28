package me.nuguri.auth.config;

import me.nuguri.common.domain.AccountAdapter;
import me.nuguri.common.enums.GrantType;
import me.nuguri.common.initializer.EntityInitializer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public TokenStore tokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * 인증 서버 엔드 포인트 /oauth/token, /oauth/check_token, /oauth/revoke_token
     * 응답 JSON 값에 추가 정보인 계정 식별키 id 값을 포함해서 응답
     * @return
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
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
    }

    @Bean
    @Profile("local")
    @ConditionalOnProperty(prefix = "${spring.jpa.hibernate.ddl-auto}", value = "create")
    public ApplicationRunner applicationRunner(EntityInitializer entityInitializer, EntityManager em) {
        return (args) -> entityInitializer.init(em);
    }

}
