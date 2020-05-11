package me.nuguri.auth.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.interceptor.AuthorityCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebServerConfiguration implements WebMvcConfigurer {

    private final AuthorityCheckInterceptor authorityCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorityCheckInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/**/login");
    }

}
