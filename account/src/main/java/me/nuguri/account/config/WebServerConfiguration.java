package me.nuguri.account.config;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.interceptor.AuthorityCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebServerConfiguration implements WebMvcConfigurer {

    private final AuthorityCheckInterceptor authorityCheckInterceptor;

    /**
     * 인터셉터 등록 및 적용 경로 매핑 설정
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry
//                .addInterceptor(authorityCheckInterceptor)
//                .addPathPatterns("/api/**");
    }

    /**
     * 리소스 경로 매핑 설정
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry
                .addResourceHandler("/vendor/**")
                .addResourceLocations("classpath:/static/vendor/");
        registry
                .addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/");
        registry
                .addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
