package me.nuguri.account.aspect;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.annotation.TokenAuthenticationUser;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.entity.Account;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenAuthenticationAspect {

    private final EntityManager entityManager;

    private final Stream<Class<? extends Annotation>> graphAnnotations = Stream.of(ManyToOne.class,
            OneToOne.class, OneToMany.class, ManyToMany.class);

    @Around("execution(* *(.., @me.nuguri.account.annotation.TokenAuthenticationUser (*), ..))")
    public Object getTokenAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Account account = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(TokenAuthenticationUser.class) &&
                    parameters[i].getType().equals(Account.class)) {
                if (account == null) {
                    String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    TokenAuthenticationUser annotation = parameters[i].getAnnotation(TokenAuthenticationUser.class);
                    String entityGraph = annotation.entityGraph();
                    EntityGraph<?> graph = null;
                    Optional<Field> optionalField = Arrays
                            .stream(Account.class.getDeclaredFields())
                            .filter(f -> f.getName().equals(entityGraph))
                            .findFirst();
                    if (optionalField.isPresent()) {
                        Field field = optionalField.get();
                        if (graphAnnotations.anyMatch(field::isAnnotationPresent)) {
                            graph = entityManager.createEntityGraph(Account.class);
                            graph.addAttributeNodes(entityGraph);
                        }
                    }
                    try {
                        TypedQuery<Account> query = entityManager
                                .createQuery("select a from Account a where a.email = :email", Account.class)
                                .setParameter("email", email);
                        if (graph != null) {
                            query.setHint("javax.persistence.loadgraph", graph);
                        }
                        account = query.getSingleResult();
                    } catch (NoResultException e) {
                        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED, "not exist account of token");
                        return ResponseEntity
                                .status(UNAUTHORIZED)
                                .body(errorResponse);
                    }
                }
                args[i] = account;
            }
        }
        return joinPoint.proceed(args);
    }

}
