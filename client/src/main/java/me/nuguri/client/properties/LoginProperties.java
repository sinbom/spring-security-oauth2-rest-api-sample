package me.nuguri.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class LoginProperties {

    private Nuguri nuguri = new Nuguri();

    @Getter
    @Setter
    public static class Nuguri {

        private String loginUrl;

        private String tokenUrl;

        private String infoUrl;

        private String clientId;

        private String clientSecret;

        private String redirectUri;
    }

}
