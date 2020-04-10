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

    /** 인증 서버 URL */
    private String remoteServerUrl;

}
