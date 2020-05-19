package me.nuguri.client.controller;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;


    @GetMapping("/nuguri/login")
    public String nuguriLogin(@RequestParam String code) {
        loginService.nuguriLogin(code);
        return "redirect:/";
    }

    @GetMapping("/naver/login")
    public String naverLogin(@RequestParam String code) {
        loginService.naverLogin(code);
        return "redirect:/";
    }

    @GetMapping("/facebook/login")
    public String facebookLogin(@RequestParam String code) {
        loginService.facebookLogin(code);
        return "redirect:/";
    }

    @GetMapping("/google/login")
    public String googleLogin(@RequestParam String code) {
        loginService.googleLogin(code);
        return "redirect:/";
    }

    @GetMapping("/kakao/login")
    public String kakaoLogin(@RequestParam String code) {
        loginService.kakaoLogin(code);
        return "redirect:/";
    }

}
