package me.nuguri.client.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StateCheckInterceptor extends HandlerInterceptorAdapter {

    /**
     * CSRF 방지용 state 값 검증
     * @param request 요청
     * @param response 응답
     * @param handler 컨트롤러 메소드
     * @return 진행 여부
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionState = (String) request.getSession().getAttribute("state");
        String state = request.getParameter("state");
        if (StringUtils.isEmpty(sessionState) || StringUtils.isEmpty(state)) {
            response.sendRedirect("/login");
            return false;
        } else {
            if (!sessionState.equals(state)) {
                response.sendRedirect("/login");
                return false;
            } else {
                return true;
            }
        }
    }

}
