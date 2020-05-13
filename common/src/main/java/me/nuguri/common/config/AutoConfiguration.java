package me.nuguri.common.config;

import me.nuguri.common.serializer.ErrorsSerializer;
import me.nuguri.common.validator.PaginationValidator;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

    @Bean
    public PaginationValidator paginationValidator() {
        return new PaginationValidator();
    }
}