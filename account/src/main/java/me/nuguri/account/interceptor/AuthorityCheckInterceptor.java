package me.nuguri.account.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.nuguri.account.annotation.HasAuthority;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.adapter.AccountAdapter;
import me.nuguri.common.dto.ErrorResponse;
import me.nuguri.common.entity.Account;
import me.nuguri.common.enums.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class AuthorityCheckInterceptor extends HandlerInterceptorAdapter {

    private final AccountRepository accountRepository;

    private final ObjectMapper objectMapper;

    /**
     * 현재 권한이 CRUD 하고자 하는 식별키가 본인의 리소스이거나 로그인 계정이 관리자 권한인지 확인
     *
     * @param request  요청
     * @param response 응답
     * @param handler  요청 매핑 핸들러
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
                Account account;

                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    account = ((AccountAdapter) authentication.getPrincipal()).getAccount();
                } else {
                    String email = (String) authentication.getPrincipal();
                    Optional<Account> optional = accountRepository.findByEmail(email);
                    if (optional.isPresent()) {
                        account = optional.get();
                    } else {
                        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED, "unauthorized");
                        response.setStatus(UNAUTHORIZED.value());
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        return false;
                    }
                }

                if (account.getId().toString().equals(id) || account.getRole().equals(Role.ADMIN)) {
                    return true;
                } else {
                    ErrorResponse errorResponse = new ErrorResponse(FORBIDDEN, "have no authority");
                    response.setStatus(FORBIDDEN.value());
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    return false;
                }
            }
        } else {
            return true;
        }
    }

}
