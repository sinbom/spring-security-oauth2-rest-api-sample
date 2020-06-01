package me.nuguri.auth.config;

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
@Configuration
@EnableWebSecurity
@Order(1) // 리소스 서버 필터 체인보다 우선순위를 높게 하여 우선적으로 시큐리티 필터 체인의 url 패턴으로 검사
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 필터 접근 이전 필터링에서 제외할 리소스 패턴 설정
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().mvcMatchers("/docs/**");
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
                .antMatchers("/", "/main").permitAll()
                .anyRequest().authenticated();
        http
                .formLogin()
                .loginPage("http://localhost:10600/login")
                .loginProcessingUrl("http://localhost:10600/login");
        http.logout().disable();
        http.httpBasic();
    }
}
