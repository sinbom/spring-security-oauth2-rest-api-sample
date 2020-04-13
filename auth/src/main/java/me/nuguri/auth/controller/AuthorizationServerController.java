package me.nuguri.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorizationServerController {

    @GetMapping(value = {"/main", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String main() {
        return "main";
    }

    /*@GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login() {
        return "login";
    }*/

}
