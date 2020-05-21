package me.nuguri.auth.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfiguration {

    private RedisServer redisServer;

    public EmbeddedRedisConfiguration(@Value("${spring.redis.port}") int port) throws IOException {
        // 레디스 실행 파일이 해당 위치에 있어야한다.
        RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
                .override(OS.WINDOWS, Architecture.x86, "/redis/redis-server.exe")
                .override(OS.WINDOWS, Architecture.x86_64, "/redis/redis-server.exe")
                .override(OS.UNIX, "")
                .override(OS.MAC_OS_X, Architecture.x86, "")
                .override(OS.MAC_OS_X, Architecture.x86_64, "");
        this.redisServer = new RedisServer(customProvider, port);
    }

    @PostConstruct
    public void postConstructor() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        if (redisServer.isActive()) {
            redisServer.stop();
        }
    }

}
