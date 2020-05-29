package me.nuguri.account.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    // TODO 로그인, 로그아웃이 REDIS 세션 로그인과 동시에 토큰까지 발급 받아서 쿠키로 가지고 있기

    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return null;
    }

}
