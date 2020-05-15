package me.nuguri.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.nuguri.auth.annotation.HasAuthority;
import me.nuguri.auth.domain.AccountAdapter;
import me.nuguri.auth.entity.Account;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthorityCheckInterceptor extends HandlerInterceptorAdapter {

    private final ObjectMapper objectMapper;

    private final TokenStore tokenStore;

    /**
     * 현재 로그인된 계정의 식별키와 CRUD 하고자 하는 식별키가 동일하거나 로그인 계정이 관리자 권한인지 확인
     * @param request 요청
     * @param response 응답
     * @param handler 요청 매핑 핸들러
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (((HandlerMethod) handler).getMethodAnnotation(HasAuthority.class) == null) {
            return true;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AccountAdapter) {
            Account account = ((AccountAdapter) principal).getAccount();
            Map<?, ?> map = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (map.get("id").equals(account.getId() + "") || account.getRoles().stream().anyMatch(r -> r.equals(Role.ADMIN))) {
                return true;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.FORBIDDEN, "have no authority")));
            return false;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.FORBIDDEN, "unauthorized")));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
