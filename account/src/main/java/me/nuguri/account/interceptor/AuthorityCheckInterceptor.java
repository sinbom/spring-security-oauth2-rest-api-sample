package me.nuguri.account.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.annotation.HasAuthority;
import me.nuguri.account.converter.CustomAuthenticationToken;
import me.nuguri.common.domain.AccountAdapter;
import me.nuguri.common.domain.ErrorResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthorityCheckInterceptor extends HandlerInterceptorAdapter {

    private final ObjectMapper objectMapper;

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
        if (handler instanceof HandlerMethod) {
            if (((HandlerMethod) handler).getMethodAnnotation(HasAuthority.class) == null) {
                return true;
            } else {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Map<String, String> map = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                String id = map.get("id");
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    Account account = ((AccountAdapter) authentication.getPrincipal()).getAccount();
                    if (account.getId().toString().equals(id) || account.getRoles().stream().anyMatch(r -> r.equals(Role.ADMIN))) {
                        return true;
                    }
                } else if (authentication instanceof OAuth2Authentication) {
//                    CustomAuthenticationToken authenticationToken = (CustomAuthenticationToken) ((OAuth2Authentication) authentication).getUserAuthentication();
//                    if (authenticationToken.getId().equals(id) || authenticationToken.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(Role.ADMIN.toString()))) {
                        return true;
//                    }
                } else {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.FORBIDDEN, "unauthorized")));
                    return false;
                }
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.FORBIDDEN, "have no authority")));
                return false;
            }
        } else {
            return true;
        }
    }

}
