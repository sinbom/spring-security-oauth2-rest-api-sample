package me.nuguri.auth.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthServerConfigProperties {

    /** 클라이언트 ID */
    private String clientId;

    /** 클라이언트 Secret */
    private String clientSecret;

}
