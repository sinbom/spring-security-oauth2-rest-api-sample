package me.nuguri.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @RequestMapping("/auth")
    public String auth() {
        return "auth";
    }

    @RequestMapping("/oauth/revoke-token")
    public ResponseEntity revokeToken() {
        return ResponseEntity.ok().build();
    }

}
