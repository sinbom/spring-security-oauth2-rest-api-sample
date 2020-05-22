package me.nuguri.account.controller.view;

import me.nuguri.common.domain.AccountAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class MainController {

    @GetMapping(value = {"/main", "/"})
    public String getSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println((AccountAdapter) authentication.getPrincipal());
        return "main";
    }

    @GetMapping("/login")
    public String login(@RequestHeader(required = false, defaultValue = "/") String referer,
                        @SessionAttribute(name = "referer", required = false) String sessionReferer, HttpSession httpSession) {
        if (StringUtils.isEmpty(sessionReferer)) {
            httpSession.setAttribute("referer", referer);
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(@RequestHeader(required = false, defaultValue = "/") String referer, HttpServletRequest request) {
        request.getSession().setAttribute("referer", referer);
        return "logout";
    }

}
