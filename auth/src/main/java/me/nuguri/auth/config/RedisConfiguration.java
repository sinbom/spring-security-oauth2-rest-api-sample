package me.nuguri.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

@Configuration
@EnableRedisHttpSession
public class RedisConfiguration extends AbstractHttpSessionApplicationInitializer {

    @Bean
    @Profile("!test")
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

}
