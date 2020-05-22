package me.nuguri.account.controller.view;

import me.nuguri.account.annotation.AuthenticationUser;
import me.nuguri.common.entity.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpSession;

@Controller
public class MainController {

    @GetMapping(value = {"/main", "/"})
    public String main() {
        return "main";
    }

    @GetMapping("/login")
    public String login(@RequestHeader(required = false, defaultValue = "/") String referer, @SessionAttribute(name = "referer", required = false) String sessionReferer,
                        @AuthenticationUser Account account, HttpSession httpSession) {
        if (account != null) {
            return "redirect:" + referer;
        }
        if (StringUtils.isEmpty(sessionReferer)) {
            httpSession.setAttribute("referer", referer);
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(@RequestHeader(required = false, defaultValue = "/") String referer, Model model) {
        model.addAttribute("referer", referer);
        return "logout";
    }

}
