package me.nuguri.auth.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class AuthorizationServerController {

    @RequestMapping({"/main", "/"})
    public String main() {
        return "main";
    }

}
