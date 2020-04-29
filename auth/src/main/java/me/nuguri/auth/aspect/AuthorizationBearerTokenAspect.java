package me.nuguri.auth.aspect;

import me.nuguri.auth.annotation.AuthorizationBearerToken;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class AuthorizationBearerTokenAspect {

    @Around("execution(* *(.., @me.nuguri.auth.annotation.AuthorizationBearerToken (*), ..))")
    public Object getBearerToken(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorization = request.getHeader("Authorization");
        Object[] args = joinPoint.getArgs();
        if (!StringUtils.isEmpty(authorization)) {
            String token = authorization.replace("Bearer", "").trim();
            Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(AuthorizationBearerToken.class) && parameters[i].getType().isAssignableFrom(String.class)) {
                    args[i] = token;
                }
            }
        }
        return joinPoint.proceed(args);
    }

}
