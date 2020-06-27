package me.nuguri.auth.aspect;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.service.RedisService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationCachingAspect {

    private final RedisService redisService;

    /**
     * 인증 토큰 발급 시, 반복적으로 발생하는 loadClientByClientId 메소드의 반환 데이터를 캐싱해서 사용
     * 레디스 캐싱을 사용하지 않는 경우, 토큰 발급 시 loadByClientId 7번, loadByUsername 1번 수행 => 총 8 조회 쿼리 발생
     * 레디스 캐싱을 사용하지 않는 경우, 토큰 검사 시 loadByClientId 2번, 총 2 조회 쿼리 발생
     *
     * @param joinPoint AOP 적용 조인 포인트
     * @return
     * @throws Throwable
     */
    @Around("execution(* me.nuguri.auth.service.AuthorizationService.loadClientByClientId(String))")
    public Object caching(ProceedingJoinPoint joinPoint) throws Throwable {
        String clientId = (String) joinPoint.getArgs()[0];
        ClientDetails clientDetails = (ClientDetails) redisService.get(clientId);
        if (clientDetails == null) {
            Object proceed = joinPoint.proceed();
            clientDetails = (ClientDetails) proceed;
            if (clientDetails != null) {
//                redisService.set(clientId, clientDetails, cachingSecond);
                redisService.set(clientId, clientDetails);
            }
            return clientDetails;
        } else {
            return clientDetails;
        }
    }


}
