package me.nuguri.auth.properties;

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

    /** authorize code 방식 로그인 리다이렉트 uri */
    private String redirectUri;

    /** 기본 생성 관리자 아이디 */
    private String adminEmail;

    /** 기본 생성 관리자 비밀번호 */
    private String adminPassword;

    /** 기본 생성 사용자 아이디 */
    private String userEmail;

    /** 기본 생성 사용자 비밀번호 */
    private String userPassword;

}
