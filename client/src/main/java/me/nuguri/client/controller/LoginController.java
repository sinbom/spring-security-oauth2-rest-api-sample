package me.nuguri.client.controller;

import lombok.RequiredArgsConstructor;
import me.nuguri.client.entity.Account;
import me.nuguri.client.properties.LoginProperties;
import me.nuguri.client.service.AccountService;
import me.nuguri.client.service.LoginService;
import me.nuguri.common.enums.Role;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    private final RestTemplate restTemplate;

    @GetMapping("/nuguri/login")
    public String oauthLogin(@RequestParam String code) {
        loginService.nuguriLogin(code);
        return "redirect:/";
    }


}
