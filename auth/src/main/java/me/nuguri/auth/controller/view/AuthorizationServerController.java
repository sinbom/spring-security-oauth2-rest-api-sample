package me.nuguri.auth.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthorizationServerController {

    @GetMapping({"/main", "/"})
    public String main() {
        return "main";
    }

}
