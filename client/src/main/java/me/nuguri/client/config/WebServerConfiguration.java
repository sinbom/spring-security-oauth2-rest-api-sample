package me.nuguri.client.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.interceptor.StateCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebServerConfiguration implements WebMvcConfigurer {

    private final StateCheckInterceptor stateCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(stateCheckInterceptor)
                .addPathPatterns("/nuguri/login", "/naver/login", "/facebook/login", "/google/login", "/kakao/login");
    }
}
