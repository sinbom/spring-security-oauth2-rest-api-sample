package me.nuguri.resource.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class ResourceServerConfigProperties {

    /** 클라이언트 ID */
    private String clientId;

    /** 클라이언트 Secret */
    private String clientSecret;

    /** 인증 서버 토큰 발급 URL */
    private String authorizeCodeUrl;

    /** 인증 서버 토큰 발급 URL */
    private String accessTokenUrl;

    /** 인증 서버 토근 만료 URL */
    private String revokeTokenUrl;

    /** 인증 서버 토근 검사 URL */
    private String checkTokenUrl;

}
