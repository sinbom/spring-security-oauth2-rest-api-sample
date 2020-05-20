package me.nuguri.account.controller;

import me.nuguri.common.domain.AccountAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping(value = {"/main", "/"})
    public String getSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println((AccountAdapter) authentication.getPrincipal());
        return "main";
    }
}
