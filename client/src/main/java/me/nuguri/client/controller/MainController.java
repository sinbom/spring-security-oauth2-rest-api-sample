package me.nuguri.client.controller;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.properties.LoginProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final LoginProperties properties;

    private final RestTemplate restTemplate;

    @GetMapping("/")
    public String main(HttpSession httpSession, Model model) {
        String state = UUID.randomUUID().toString();
        model.addAttribute("nuguriLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getNuguri().getLoginUrl())
                .queryParam("client_id", properties.getNuguri().getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", properties.getNuguri().getRedirectUri())
                .queryParam("state", state)
                .build()
                .toString());
        model.addAttribute("naverLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getNaver().getLoginUrl())
                .queryParam("client_id", properties.getNaver().getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", properties.getNaver().getRedirectUri())
                .queryParam("state", state)
                .build()
                .toString());
        model.addAttribute("facebookLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getFacebook().getLoginUrl())
                .queryParam("client_id", properties.getFacebook().getClientId())
                .queryParam("redirect_uri", properties.getFacebook().getRedirectUri())
                .queryParam("state", state)
                .build()
                .toString());
        model.addAttribute("googleLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getGoogle().getLoginUrl())
                .queryParam("client_id", properties.getGoogle().getClientId())
                .queryParam("redirect_uri", properties.getGoogle().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile")
                .queryParam("state", state)
                .build()
                .toString());
        model.addAttribute("kakaoLoginUri", UriComponentsBuilder
                .fromHttpUrl(properties.getKakao().getLoginUrl())
                .queryParam("client_id", properties.getKakao().getClientId())
                .queryParam("redirect_uri", properties.getKakao().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build()
                .toString());

        httpSession.setAttribute("state", state);
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
