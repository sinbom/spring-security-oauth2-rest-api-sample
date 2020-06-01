package me.nuguri.account.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(1) // 리소스 서버 필터 체인보다 우선순위를 높게 하여 우선적으로 시큐리티 필터 체인의 url 패턴으로 검사
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final SecurityExpressionHandler<FilterInvocation> customExpressionHandler;

    /**
     * 필터 접근 이전 필터링에서 제외할 리소스 패턴 설정
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/**");
        web.ignoring().mvcMatchers("/vendor/**");
        web.ignoring().mvcMatchers("/fonts/**");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * 시큐리티(리소스 X) 필터 체인 설정, /api/** url 패턴이 아닌 경우 권한 처리
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .regexMatchers("^(?!/api/).*$")
                .and()
                .authorizeRequests()
                .mvcMatchers("/", "/main").permitAll()
                .anyRequest().authenticated()
                .expressionHandler(customExpressionHandler);
        http
                .formLogin()
                .loginPage("/login")
                .successHandler(new SimpleUrlAuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        String referer = (String) request.getSession().getAttribute("referer");
                        referer = StringUtils.isEmpty(referer) ? "/" : referer;
                        response.sendRedirect(referer);
                        super.onAuthenticationSuccess(request, response, authentication);
                        log.info("[log] login referer : " + referer);
                        log.info("[log] username : " + authentication.getName() + " is login success at " + LocalDateTime.now());
                    }
                })
                .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        super.onAuthenticationFailure(request, response, exception);
                        log.info("[log] username : " + request.getParameter("username") + " is login fail at " + LocalDateTime.now());
                    }
                })
                .permitAll();
        http
                .logout()
                .logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        String referer = request.getParameter("referer");
                        referer = StringUtils.isEmpty(referer) ? "/" : referer;
                        log.info("[log] logout referer : " + referer);
                        response.sendRedirect(referer);
                        super.onLogoutSuccess(request, response, authentication);
                        log.info("[log] username : " + authentication.getName() + " is logout at " + LocalDateTime.now());
                    }
                })
                .logoutUrl("/logout");
        http.httpBasic();
        http.rememberMe().rememberMeParameter("remember-me").userDetailsService(userDetailsService).rememberMeCookieName("remember-me");
        http.sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(false).expiredUrl("/login");
    }


}
