package me.nuguri.auth.aspect;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.annotation.Oauth2Authentication;
import me.nuguri.auth.annotation.Oauth2AccessToken;
import me.nuguri.common.domain.ErrorResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final TokenStore tokenStore;

    /**
     * Authorization Header로 전달 받은 Bearer 토큰으로 엑세스 토큰 추출
     * AOauth2AccessToken 애노테이션 적용된 파라미터가 있는 컨트롤러에 적용
     * @param joinPoint AOP 적용 메소드 조인 포인트
     * @return
     * @throws Throwable
     */
    @Around("execution(* *(.., @me.nuguri.auth.annotation.Oauth2AccessToken (*), ..))")
    public Object getAccessToken(ProceedingJoinPoint joinPoint) throws Throwable {
        String access_token = extractBearerToken();
        Object[] args = joinPoint.getArgs();
        if (StringUtils.isEmpty(access_token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "no bearer token in authorization header"));
        } else {
            OAuth2AccessToken oauth2AccessToken = tokenStore.readAccessToken(access_token);
            if (oauth2AccessToken != null) {
                Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(Oauth2AccessToken.class) && OAuth2AccessToken.class.isAssignableFrom(parameters[i].getType())) {
                        args[i] = oauth2AccessToken;
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "unauthorized bearer token"));
            }
        }
        return joinPoint.proceed(args);
    }

    /**
     * Authorization Header로 전달 받은 Bearer 토큰으로 인증 객체 추출
     * Oauth2Authentication 애노테이션 적용된 파라미터가 있는 컨트롤러에 적용
     * @param joinPoint AOP 적용 메소드 조인 포인트
     * @return AOP 적용 메소드 리턴 값
     * @throws Throwable
     */
    @Around("execution(* *(.., @me.nuguri.auth.annotation.Oauth2Authentication (*), ..))")
    public Object getAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        String access_token = extractBearerToken();
        Object[] args = joinPoint.getArgs();
        if (StringUtils.isEmpty(access_token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "no bearer token in authorization header"));
        } else {
            OAuth2Authentication authentication = tokenStore.readAuthentication(access_token);
            if (authentication != null) {
                Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(Oauth2Authentication.class) && OAuth2Authentication.class.isAssignableFrom(parameters[i].getType())) {
                        args[i] = authentication;
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "unauthorized bearer token"));
            }
        }
        return joinPoint.proceed(args);
    }

    /**
     * Authorization Header로 전달 받은 Bearer 토큰 값을 추출
     * @return Bearer 토큰
     */
    private String extractBearerToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.isEmpty(authorization) && authorization.contains("Bearer ")) {
            return authorization.replace("Bearer ", "").trim();
        } else {
            return null;
        }
    }

}
