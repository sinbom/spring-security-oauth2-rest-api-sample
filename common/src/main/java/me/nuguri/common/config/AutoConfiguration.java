package me.nuguri.common.config;

import me.nuguri.common.support.EntityInitializer;
import me.nuguri.common.support.PaginationValidator;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AutoConfiguration {

    @Bean
    public PaginationValidator paginationValidator() { return new PaginationValidator(); }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public EntityInitializer entityInitializer() {
        return new EntityInitializer(passwordEncoder());
    }

}
