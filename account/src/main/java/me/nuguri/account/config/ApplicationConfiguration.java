package me.nuguri.account.config;

import me.nuguri.common.initializer.EntityInitializer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityManager;

@Configuration
@EnableJpaAuditing
public class ApplicationConfiguration {

    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
    public ApplicationRunner applicationRunner(EntityInitializer entityInitializer, EntityManager em) {
        return (args) -> entityInitializer.init(em);
    }

}
