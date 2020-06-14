package me.nuguri.resc.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.nuguri.resc.repository.BaseRepository;
import me.nuguri.resc.repository.impl.BaseRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackageClasses = BaseRepository.class, repositoryBaseClass = BaseRepositoryImpl.class)
public class ApplicationConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
