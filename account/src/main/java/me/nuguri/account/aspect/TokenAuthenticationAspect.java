package me.nuguri.account.aspect;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.annotation.TokenAuthenticationUser;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.entity.Account;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenAuthenticationAspect {

    private final AccountRepository accountRepository;

    @Around("execution(* *(.., @me.nuguri.account.annotation.TokenAuthenticationUser (*), ..))")
    public Object getTokenAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Account account = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(TokenAuthenticationUser.class) && parameters[i].getType().equals(Account.class)) {
                if (account == null) {
                    String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    Optional<Account> optional = accountRepository.findByEmail(email);
                    if (optional.isPresent()) {
                        account = optional.get();
                    } else {
                        ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "not exist account of token");
                        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
                    }
                }
                args[i] = account;
            }
        }
        return joinPoint.proceed(args);
    }

}
