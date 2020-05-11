package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.domain.AccountAdapter;
import me.nuguri.common.enums.GrantType;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final DataSource dataSource;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

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

}
