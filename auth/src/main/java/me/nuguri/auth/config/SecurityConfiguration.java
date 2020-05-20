package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
//TODO 추후 Auth, Account 서버 분리 후 로그인 세션 키 Redis 클러스터링 하기
@Configuration
@EnableWebSecurity
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().mvcMatchers("/docs/**");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .regexMatchers("^(?!/api/).*$")
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/", "/main", "/oauth/me").permitAll()
                .antMatchers("/oauth/revoke_token").permitAll()
                .anyRequest().authenticated();
        http.csrf().ignoringAntMatchers("/oauth/revoke_token", "/oauth/me");
        http.httpBasic().disable();
        http.formLogin().loginPage("http://localhost:10600/login");
        http.logout().disable();
    }
}
