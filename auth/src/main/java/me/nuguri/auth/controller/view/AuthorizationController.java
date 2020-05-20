package me.nuguri.auth.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class AuthorizationController {

//    private final RedisTemplate<String, Object> redisTemplate;

    private final StringRedisTemplate redisTemplate;

    @RequestMapping({"/main", "/"})
    public String main(HttpServletRequest request) {
        System.out.println(redisTemplate.opsForValue().get("apptest"));
        Cookie[] cookie = request.getCookies();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        for (Cookie cookie1 : cookie) {
            System.out.println(cookie1.getName());
            System.out.println(cookie1.getValue());
        }
        return "main";
    }

}
