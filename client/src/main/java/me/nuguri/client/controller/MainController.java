package me.nuguri.client.controller;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.properties.LoginProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final LoginProperties properties;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("nuguriLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getNuguri().getLoginUrl())
                .queryParam("client_id", properties.getNuguri().getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", properties.getNuguri().getRedirectUri())
                .build()
                .toString());
        return "main";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }


}
