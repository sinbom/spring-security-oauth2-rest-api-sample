package me.nuguri.account.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "account")
@Getter
@Setter
public class AccountServerProperties {

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

    /** 인증 서버 토큰 발급 URL */
    private String authorizeCodeUrl;

    /** 인증 서버 토큰 발급 URL */
    private String accessTokenUrl;

    /** 인증 서버 토근 만료 URL */
    private String revokeTokenUrl;

    /** 인증 서버 토근 검사 URL */
    private String checkTokenUrl;

}
