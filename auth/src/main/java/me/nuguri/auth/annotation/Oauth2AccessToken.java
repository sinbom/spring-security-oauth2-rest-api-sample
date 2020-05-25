package me.nuguri.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증 서버 url 패턴 필터 체인에 해당하지 않는 경우 애노테이션으로 인증 엑세스 토큰 주입
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy. RUNTIME)
public @interface Oauth2AccessToken {
}
