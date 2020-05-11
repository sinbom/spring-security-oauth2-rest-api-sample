package me.nuguri.resc.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "test")
@Getter
@Setter
public class TestProperties {

    /** 기본 생성 관리자 아이디 */
    private String adminEmail;

    /** 기본 생성 관리자 비밀번호 */
    private String adminPassword;

    /** 기본 생성 사용자 아이디 */
    private String userEmail;

    /** 기본 생성 사용자 비밀번호 */
    private String userPassword;

}
