package me.nuguri.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nuguri.auth.property.AuthServerConfigProperties;
import me.nuguri.auth.service.ClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final ClientService clientService;

    private final AuthServerConfigProperties properties;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Override
    public void run(ApplicationArguments args) {
        if (profile.equals("local") && ddlAuto.equals("create")) {
            log.info("[log] [active profile is " + profile + "] => do persist test entities");
            /** persist code */
        } else {
            log.info("[log] [active profile is " + profile + "] => do not persist test entities");
        }
    }

}
